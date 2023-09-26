package com.chitchat.common.ui.screens.conversation

import com.chitchat.common.getPlatformSpecificEvent
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.chitchat.common.model.ChatMessage
import com.chitchat.common.model.EventType
import com.chitchat.common.model.PlatformEvent
import com.chitchat.common.model.VoskResult
import com.chitchat.common.model.toShortLocalDateTime
import com.chitchat.common.model.toShortLocalTime
import kotlinx.datetime.Clock


@Serializable
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGettingAnswer: Boolean = false,
    val isListening: Boolean = false
)

@Serializable
data class GptAnswer(
    val answer: String,
    val id: String
)

class ConversationViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()
    val httpClient: HttpClient = HttpClient(CIO) {
        this.install(ContentNegotiation) {
            json()
        }
    }

    fun watch(msg: StateFlow<PlatformEvent>) = this.viewModelScope.launch {
            msg.collect { onNewMessage(it) }
        }

    private fun onNewMessage(msg: PlatformEvent) {
        this.viewModelScope.launch {
            if (msg.eType == "Recognizer") {
                when {
                    msg.message.isNotEmpty() -> {
                        val newMessage = Json.decodeFromString(VoskResult.serializer(), msg.message)
                        val current = _uiState.value.messages.toMutableList()
                        if ((newMessage.partial != null && newMessage.partial.isBlank())) {
                            // new paragraph
                            if (current.lastOrNull()?.message?.isNotBlank() == true)
                                current.add(ChatMessage(message = newMessage.partial,
                                    endTime = Clock.System.now().toShortLocalTime()))
                        } else {
                            // update last text
                            if (newMessage.text == null) {
                                newMessage.partial?.let {
                                    var time = Clock.System.now().toShortLocalTime()
                                    if (current.isNotEmpty()) {
                                        time = current.last().time
                                        current.removeLast()
                                    }
                                    current.add(ChatMessage(message = it, time = time,
                                        endTime = Clock.System.now().toShortLocalTime()))
                                }
                            } else {
                                // Add an empty row to the end
                                if (current.lastOrNull()?.message?.isNotBlank() == true)
                                    current.add(ChatMessage(message = "",
                                        endTime = Clock.System.now().toShortLocalTime()))
                            }
                        }
                        _uiState.update { it.copy(messages = current.toList()) }
                    }

                    msg.message.isEmpty() -> { // empty string means new paragraph
                        val all = _uiState.value.messages.toMutableList()
                        all.add(ChatMessage(message = "", user = "audience",
                            endTime = Clock.System.now().toShortLocalTime()))
                        _uiState.update { it.copy(messages = all.toList()) }
                    }

                    msg.error != null -> {
                        // show error
                    }
                }
            }
        }
    }

    fun onListenToggle() {
        val isListening = _uiState.value.isListening
        if (!isListening)
            getPlatformSpecificEvent().startListen()
        else
            getPlatformSpecificEvent().stopListen()
        _uiState.update { it.copy(isListening = !isListening) }
    }

    fun onShare() {
        val allText = _uiState.value.messages.map { it.message }.joinToString(separator = "\n")
        getPlatformSpecificEvent().shareAsTextFile(
            allText, "file"
            )// Clock.System.now().toShortLocalDateTime(true)
    }

    fun onSummarize() {

    }

    fun onGptAnswer() {
        // Call backend
        getPlatformSpecificEvent().startPurchase()
        this.viewModelScope.launch {
            isGettingAnswer(true)
            val response = httpClient.get("https://mocki.io/v1/73ff907f-ffe2-4749-84b3-d1208c992e77") {
                contentType(ContentType.Application.Json)
            }
            if (response.status == HttpStatusCode.OK) {
                val answer: GptAnswer = response.body()
                isGettingAnswer(false)
                addMessageList(ChatMessage(message = answer.answer,
                    user = "AI",
                    endTime = Clock.System.now().toShortLocalTime()))
            }
        }
    }

    private fun addMessageList(msg: ChatMessage) {
        _uiState.update {
            it.copy(messages = _uiState.value.messages.toMutableList().apply { add(msg) }.toList())
        }
    }

    private fun isGettingAnswer(value: Boolean) {
        _uiState.update { it.copy(isGettingAnswer = value) }
    }
}