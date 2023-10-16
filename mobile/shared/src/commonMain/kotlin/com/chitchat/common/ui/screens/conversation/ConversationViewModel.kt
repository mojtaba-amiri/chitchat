package com.chitchat.common.ui.screens.conversation

import com.chitchat.common.getPlatformName
import com.chitchat.common.getPlatformSpecificEvent
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.chitchat.common.model.ChatMessage
import com.chitchat.common.model.PlatformEvent
import com.chitchat.common.model.VoskResult
import com.chitchat.common.model.asFileName
import com.chitchat.common.model.toShortLocalDateTime
import com.chitchat.common.model.toShortLocalTime
import com.chitchat.common.repository.ChatRepository
import com.chitchat.common.settings
import io.github.aakira.napier.Napier
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName


@Serializable
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGettingAnswer: Boolean = false,
    val isGettingSummary: Boolean = false,
    val isListening: Boolean = false,
    val displayMessage: String? = null
)

@Serializable
data class RegisterDto(
    @SerialName("user_id") val userId: String,
    @SerialName("req_platform") val reqPlatform: String
)

@Serializable
data class RegisterResponse(@SerialName("access_token") val accessToken: String)

@Serializable
data class GptAnswer(
    val response: GptResponse
)

@Serializable
data class GptResponse(
    val choices: List<Choice>,
    val model: String
)

@Serializable
data class Choice(
    val message: GptMessage
)

@Serializable
data class GptMessage(
    val role: String,
    val content: String
)

/**
 * {
 *     "id": "chatcmpl-abc123",
 *     "object": "chat.completion",
 *     "created": 1677858242,
 *     "model": "gpt-3.5-turbo-0613",
 *     "usage": {
 *         "prompt_tokens": 13,
 *         "completion_tokens": 7,
 *         "total_tokens": 20
 *     },
 *     "choices": [
 *         {
 *             "message": {
 *                 "role": "assistant",
 *                 "content": "\n\nThis is a test!"
 *             },
 *             "finish_reason": "stop",
 *             "index": 0
 *         }
 *     ]
 * }
 * */

class ConversationViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()
    private val repo = ChatRepository()
    private var userId = settings.getString("UserId", "")

    fun watch(msg: StateFlow<PlatformEvent>) = this.viewModelScope.launch {
            msg.collect { onNewMessage(it) }
        }

    private fun onNewMessage(msg: PlatformEvent) {
        this.viewModelScope.launch {
            if (msg.error != null) {
                handleErrorMessage(msg)
                return@launch
            }
            when (msg.eType) {
                "Recognizer" -> {
                    handleNameTranscribe(msg)
                }
                "PremiumAccess" -> {
                    setUser(msg.message)
                    refreshToken(msg.message)
                }
                "UserId" -> {
                    setUser(msg.message)
                }
            }
        }
    }

    private fun handleErrorMessage(msg: PlatformEvent) {

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

    private fun refreshToken(userId: String, callGetAnswer: Boolean = false) {
        this.viewModelScope.launch {
            try {
                val response = repo.refreshToken(userId)

                if (response.status == HttpStatusCode.OK) {
                    val result: RegisterResponse = response.body()
                    repo.setAuthToken(result.accessToken)
                    if (callGetAnswer) onGptAnswer()
                } else {
                    if (getPlatformSpecificEvent().hasPremium()) {
                        // User is premium but getting token failed
                    } else {
                        // Register failed because user is not Premium
                    }
                    // Register failed no token
                }
            } catch (e: HttpRequestTimeoutException) {
                _uiState.update {
                    it.copy(displayMessage = "Server is busier than usual. try again few moments later.")
                }
                Napier.e("TimeOut Exception")
            }
        }
    }

    private fun setUser(userId: String) {
        settings.putString("UserId", userId)
        this.userId = userId
    }

    fun onListenToggle() {
        val isListening = _uiState.value.isListening
        if (!isListening) {
            getPlatformSpecificEvent().startListen()
            _uiState.update {
                it.copy(displayMessage = "Initializing..")
            }
        }
        else {
            getPlatformSpecificEvent().stopListen()
        }
        _uiState.update { it.copy(isListening = !isListening) }
    }

    fun onShare() {
        val allText = _uiState.value.messages.joinToString(separator = "\n") { it.message }
        val fileName = Clock.System.now().asFileName()
        getPlatformSpecificEvent().shareAsTextFile(allText, fileName)
    }

    fun onSummarize() {
        // Call backend
        if (getPlatformSpecificEvent().hasPremium()) {
            this.viewModelScope.launch {

                isGettingSummary(true)
                try {
                    val response = repo.summarize()
                    if (response.status == HttpStatusCode.OK) {
                        val answer: GptAnswer = response.body()
                        addMessageList(ChatMessage(message =
                        answer.response.choices.map { it.message.content }.joinToString ("\n"),
                            user = "AI",
                            endTime = Clock.System.now().toShortLocalTime()))
                    }
                }
                catch (e: HttpRequestTimeoutException) {
                    _uiState.update {
                        it.copy(displayMessage = "Server is busier than usual. try again few moments later.")
                    }
                    Napier.e("TimeOut Exception")
                }
                isGettingSummary(false)

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
                try {
                    val response = repo.answer(_uiState.value.messages.takeLast(7))
                    if (response == null) {
                        getPlatformSpecificEvent().startPurchase()
                        return@launch
                    }
                    if (response.status == HttpStatusCode.OK) {
                        val result: GptAnswer = response.body()
                        addMessageList(
                            ChatMessage(message = result
                                .response
                                .choices.map { it.message.content }.joinToString ("\n" ),
                                user = "AI",
                                endTime = Clock.System.now().toShortLocalTime()))
                    }
                    if (response.status == HttpStatusCode.Unauthorized) {
                        refreshToken(userId, true)
                    }
                } catch (e: HttpRequestTimeoutException) {
                    _uiState.update {
                        it.copy(displayMessage = "Server is busier than usual. try again few moments later.")
                    }
                    Napier.e("TimeOut Exception")
                }
                isGettingAnswer(false)

            }
        } else {
            getPlatformSpecificEvent().startPurchase()
        }
    }

    fun messageShown() {
        _uiState.update {
            it.copy(displayMessage = null)
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


    private fun isGettingSummary(value: Boolean) {
        _uiState.update { it.copy(isGettingSummary = value) }
    }
}