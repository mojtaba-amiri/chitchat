import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import model.ChatMessage
import model.PlatformEvent
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import viewModel.ChatViewModel

var newEvent: PlatformEvent = PlatformEvent()

@Composable
fun App(newEvent: StateFlow<PlatformEvent>) {
    MaterialTheme {
        val viewModel = getViewModel(Unit, viewModelFactory { ChatViewModel() })
        viewModel.watch(newEvent)
//        val scope = rememberCoroutineScope()
//        LaunchedEffect(newEvent) {
//            scope.launch {
//                viewModel.onNewMessage(newEvent.value)
//            }
//        }
        MainPage(viewModel)
    }
}

fun onMessageReceived(message: String) {
    newEvent = PlatformEvent(message)
}

fun onRecognizerError(e: Exception) {
    newEvent = PlatformEvent(error = e)
}

fun onRecognizerTimeOut() {
    newEvent = PlatformEvent(timeout = true)
}

@OptIn(ExperimentalResourceApi::class)
@Composable fun MainPage(viewModel: ChatViewModel) {
    var greetingText by remember { mutableStateOf("Hello, World!") }
    var showImage by remember { mutableStateOf(false) }
    val uiState = viewModel.uiState.collectAsState()

    Column(Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally) {

        Row {
            Button(
                onClick = { getPlatformSpecificEvent().startListen() }
            ) {
                Text(text = "Start")
            }

            Button(
                onClick = { getPlatformSpecificEvent().stopListen() }
            ) {
                Text(text = "Stop")
            }
        }
        val lazyColumnListState = rememberLazyListState()
        val corroutineScope = rememberCoroutineScope()

        LazyColumn(
            state = lazyColumnListState
        ) {
            items(uiState.value.messages) {
                MessageCard(it)
            }
            corroutineScope.launch {
                if (uiState.value.messages.isNotEmpty()) lazyColumnListState.scrollToItem(uiState.value.messages.size - 1)
            }
        }
//
//        Button(onClick = {
//            greetingText = "Hello, ${getPlatformName()}"
//            showImage = !showImage
//        }) {
//            Text(greetingText)
//        }
//        AnimatedVisibility(showImage) {
//            Image(
//                painterResource("compose-multiplatform.xml"),
//                null
//            )
//        }
    }
}

@Composable fun MessageCard(msg: ChatMessage) {
    Logger.i { "Compose: ${msg.message}" }
    Card(
        backgroundColor = MaterialTheme.colors.background,
        elevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Text(
            text = msg.message
        )
    }
}

expect fun getPlatformName(): String
expect fun getPlatformSpecificEvent(): PlatformSpecificEvent
expect class PlatformSpecificEvent {
    fun startListen()
    fun stopListen()
}