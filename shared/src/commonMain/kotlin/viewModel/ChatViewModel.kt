package viewModel

import co.touchlab.kermit.Logger
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import model.ChatMessage
import model.PlatformEvent
import model.VoskResult


@Serializable
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList()
)

class ChatViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()
//
//    fun onNewEvent(msg: String = "", e: Exception? = null, timeOut: Boolean = false) {
//
//    }

    fun watch(msg: StateFlow<PlatformEvent>) {
        this.viewModelScope.launch{
            msg.collect {
                onNewMessage(it)
            }
        }
    }

    fun onNewMessage(msg: PlatformEvent) {
        this.viewModelScope.launch {
            when {
                msg.message.isNotEmpty() -> {
                    Logger.i {msg.message}
                    val newMessage = Json.decodeFromString(VoskResult.serializer(), msg.message)
                    val current = _uiState.value.messages.toMutableList()
                    if ((newMessage.partial != null && newMessage.partial.isBlank())) {
                        // new paragraph
                        current.add(ChatMessage(message = newMessage.partial))
                    } else {
                        // update last text
                        if (newMessage.text == null) {
                            newMessage.partial?.let {
                                if (current.isNotEmpty()) current.removeLast()
                                current.add(ChatMessage(message = it))
                            }
                        } else {
                            current.add(ChatMessage(message = ""))
                        }
                    }
                    _uiState.update { it.copy(messages = current.toList()) }
                }

                msg.message.isEmpty() -> { // empty string means new paragraph
                    val all = _uiState.value.messages.toMutableList()
                    all.add(ChatMessage(message = "", user = "audience"))
                    _uiState.update { it.copy(messages = all.toList()) }
                }

                msg.error != null -> {
                    // show error
                }
            }
        }
    }
}