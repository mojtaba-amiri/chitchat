package com.chitchat.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.chitchat.common.model.ChatMessage
import com.chitchat.common.model.PlatformEvent
import com.chitchat.common.viewModel.ChatUiState
import org.jetbrains.compose.resources.ExperimentalResourceApi
import com.chitchat.common.viewModel.ChatViewModel

var newEvent: PlatformEvent = PlatformEvent()

@Composable
fun App(newEvent: StateFlow<PlatformEvent>) {
    MaterialTheme {
        val viewModel = getViewModel(Unit, viewModelFactory { ChatViewModel() })
        viewModel.watch(newEvent)
        MainPage(Modifier, viewModel)
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
@Composable fun MainPage(modifier: Modifier = Modifier, viewModel: ChatViewModel) {
    val uiState = viewModel.uiState.collectAsState()

    Column(
        modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = Color.Black),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.Start) {


        ConversationList(uiState)
        ActionsLayout(uiState, viewModel)
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
//
//@Composable fun ConversationScreen(list: @Composable ColumnScope.() -> Unit,
//    actionsLayout: @Composable (ChatUiState, ChatViewModel)-> Unit
//) {
//
//}

@Composable fun ColumnScope.ConversationList(uiState: State<ChatUiState>) {
    val lazyColumnListState = rememberLazyListState()
    val corroutineScope = rememberCoroutineScope()
    LazyColumn(
        state = lazyColumnListState,
        modifier = Modifier
            .padding(5.dp)
            .weight(1f)
    ) {
        items(uiState.value.messages) {
            MessageCard(it)
        }
        corroutineScope.launch {
            if (uiState.value.messages.isNotEmpty())
                lazyColumnListState.scrollToItem(uiState.value.messages.size - 1)
        }
    }
}

@Composable fun ActionsLayout(uiState: State<ChatUiState>, viewModel: ChatViewModel) {
    Row(
        modifier= Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround) {
        Button(
            modifier = Modifier.width(150.dp),
            onClick = { viewModel.onListenToggle() }
        ) {
            Text(text = if (uiState.value.isListening) stringResource(MR.strings.end_interview) else stringResource(MR.strings.start_interview))
        }

        Button(
            modifier = Modifier.width(150.dp),
            onClick = { viewModel.onGptAnswer() }
        ) {
            Text(text = if(uiState.value.isGettingAnswer) stringResource(MR.strings.getting_answer)   else stringResource(MR.strings.gpt_answer))
        }
    }
}

@Composable fun MessageCard(msg: ChatMessage) {
//    Logger.i { "Compose: ${msg.message}" }
    Card(
        contentColor = if (msg.user == "audience") Color.LightGray else Color.Green,
        elevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Text(
            modifier = Modifier
                .padding(16.dp),
            text = msg.message.ifBlank { "..." },
            color = Color.DarkGray,
            style = MaterialTheme.typography.body1
        )
    }
}