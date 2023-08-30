package viewModel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.ChatMessage
import model.PlatformEvent


data class ChatUiState(
    val messages: List<ChatMessage> = emptyList()
)

class ChatViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    fun onNewMessage(msg: PlatformEvent) {
        when {
            msg.message.isNotEmpty() -> {
                if (_uiState.value.messages.isEmpty())
                    _uiState.tryEmit(
                        ChatUiState(listOf(ChatMessage(message = msg.message, user = "audience"))))
                else {
                    _uiState.value.messages.last().message = msg.message
                    _uiState.tryEmit(_uiState.value.copy())
                }
            }
            msg.message.isEmpty() -> { // empty string means new paragraph
                val all = mutableListOf<ChatMessage>()
                all.addAll(_uiState.value.messages)
                all.add(ChatMessage(message = "", user = "audience"))
                _uiState.tryEmit(ChatUiState(all.toList()))
            }
            msg.error != null -> {
                // show error
            }
        }
    }
//    val httpClient: HttpClient by inject()
//    val
}