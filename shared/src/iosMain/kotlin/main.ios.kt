import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import model.ChatMessage

actual fun getPlatformName(): String = "iOS"

actual fun getPlatformSpecificEvent() = PlatformSpecificEvent()

actual class PlatformSpecificEvent{
    companion object {
        var startRecognizer: (() -> Unit)? = null
        var stopRecognizer: (() -> Unit)? = null
    }
    actual fun startListen() {
        startRecognizer?.invoke()
    }
    actual fun stopListen() {
        stopRecognizer?.invoke()
    }
}

fun MainViewController() = ComposeUIViewController { App() }