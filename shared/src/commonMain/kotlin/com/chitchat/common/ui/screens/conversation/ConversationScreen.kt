package com.chitchat.common.ui.screens.conversation

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.chitchat.common.MR
import com.chitchat.common.model.ChatMessage
import com.chitchat.common.model.PlatformEvent
import com.chitchat.common.ui.navigator.NavigatorViewModel
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi


@OptIn(ExperimentalResourceApi::class)
@Composable
fun ConversationScreen(modifier: Modifier = Modifier,
                       platformEvent: StateFlow<PlatformEvent>,
                       navigator: NavigatorViewModel) {
    val viewModel = getViewModel("Conversation", viewModelFactory { ConversationViewModel() })
    val uiState = viewModel.uiState.collectAsState()
    viewModel.watch(platformEvent)
    Column(
        modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = Color.White),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.Start) {


        ConversationList(uiState)
        ActionsLayout(uiState, viewModel)
    }
}

@Composable
fun ColumnScope.ConversationList(uiState: State<ChatUiState>) {
    val lazyColumnListState = rememberLazyListState()
    val corroutineScope = rememberCoroutineScope()
    LazyColumn(
        state = lazyColumnListState,
        modifier = Modifier
            .padding(30.dp)
            .weight(1f)
    ) {
        items(uiState.value.messages) {
            MessageCard(it)
            TimeStamp(it)
        }
        corroutineScope.launch {
            if (uiState.value.messages.isNotEmpty())
                lazyColumnListState.scrollToItem(uiState.value.messages.size - 1)
        }
    }
}

@Composable
fun ActionsLayout(uiState: State<ChatUiState>, viewModel: ConversationViewModel) {
    Row(
        modifier= Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround) {
        Button(
            modifier = Modifier.width(150.dp),
            onClick = { viewModel.onListenToggle() }
        ) {
            Text(text = if (uiState.value.isListening) stringResource(MR.strings.end_interview) else stringResource(
                MR.strings.start_interview)
            )
        }

        Button(
            modifier = Modifier.width(150.dp),
            onClick = { viewModel.onGptAnswer() }
        ) {
            Text(text = if(uiState.value.isGettingAnswer) stringResource(MR.strings.getting_answer)   else stringResource(
                MR.strings.gpt_answer)
            )
        }
    }
}

@Composable
fun MessageCard(msg: ChatMessage) {
//    Logger.i { "Compose: ${msg.message}" }
    Card(
        backgroundColor = colorResource(if (msg.user == "AI") MR.colors.primaryColor  else MR.colors.cardColor),
        elevation = 3.dp,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 0.dp, bottomStart = 30.dp, bottomEnd = 30.dp),
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

@Composable
fun TimeStamp(msg: ChatMessage) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                modifier = Modifier
                    .background(Color.White, shape = RoundedCornerShape(5.dp))
                    .padding(6.dp),
                text = msg.time.ifBlank { "--" },
                fontSize = 14.sp,
                color = Color.LightGray,
                style = MaterialTheme.typography.body1
            )
        }
}