package viewModel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.ChatMessage


data class ChatUiState(
    val messages: List<ChatMessage> = emptyList()
)

class ChatViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()
//    val httpClient: HttpClient by inject()
//    val
}