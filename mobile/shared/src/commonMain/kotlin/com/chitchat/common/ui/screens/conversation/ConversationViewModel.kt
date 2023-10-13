package com.chitchat.common.ui.screens.conversation

import com.chitchat.common.ANSWER_ENDPOINT
import com.chitchat.common.BASE_URL
import com.chitchat.common.REGISTER_ENDPOINT
import com.chitchat.common.SUMMARIZE_ENDPOINT
import com.chitchat.common.TRANSCRIBE_ENDPOINT
import com.chitchat.common.getPlatformName
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
import com.chitchat.common.repository.ChatRepository
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName


@Serializable
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGettingAnswer: Boolean = false,
    val isListening: Boolean = false
)

@Serializable
data class RegisterDto(
    @SerialName("user_id") val userId: String,
    val platform: String = getPlatformName()
)

@Serializable
data class RegisterResponse(@SerialName("access_token") val accessToken: String)

@Serializable
data class GptAnswer(
    val answer: String,
    val id: String
)

class ConversationViewModel: ViewModel() {
    private var accessToken: String? = null
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()
    val repo = ChatRepository()

    fun watch(msg: StateFlow<PlatformEvent>) = this.viewModelScope.launch {
            msg.collect { onNewMessage(it) }
        }

    private fun onNewMessage(msg: PlatformEvent) {
        this.viewModelScope.launch {
            when (msg.eType) {
                "Recognizer" -> { handleNameTranscribe(msg) }
                "PremiumAccess" -> { handlePremiumAccess(msg.message) }
            }
        }
    }

    private fun handleNameTranscribe(msg: PlatformEvent) {
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

    private fun handlePremiumAccess(userId: String) {
        this.viewModelScope.launch {
            isGettingAnswer(true)
            val response = repo.registerUser(userId)
            isGettingAnswer(false)
            if (response.status == HttpStatusCode.OK) {
                val result: RegisterResponse = response.body()
                this@ConversationViewModel.accessToken = result.accessToken
                onGptAnswer()
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
        // Call backend
        if (getPlatformSpecificEvent().hasPremium()) {
            this.viewModelScope.launch {
                isGettingAnswer(true)
                val response = repo.summarize()
                isGettingAnswer(false)
                if (response.status == HttpStatusCode.OK) {
                    val answer: GptAnswer = response.body()
                    addMessageList(ChatMessage(message = answer.answer,
                        user = "AI",
                        endTime = Clock.System.now().toShortLocalTime()))
                }
            }
        } else {
            getPlatformSpecificEvent().startPurchase()
        }

    }

    fun onGptAnswer() {
        // Call backend
        if (getPlatformSpecificEvent().hasPremium()) {
            this.viewModelScope.launch {
                isGettingAnswer(true)
                val response = repo.answer(_uiState.value.messages)
                isGettingAnswer(false)
                if (response.status == HttpStatusCode.OK) {
                    val answer: GptAnswer = response.body()
                    addMessageList(ChatMessage(message = answer.answer,
                        user = "AI",
                        endTime = Clock.System.now().toShortLocalTime()))
                }
            }
        } else {
            getPlatformSpecificEvent().startPurchase()
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