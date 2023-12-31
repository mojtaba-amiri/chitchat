package com.chitchat.common.ui.screens.conversation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chitchat.common.MR
import com.chitchat.common.model.ChatMessage
import com.chitchat.common.model.PlatformEvent
import com.chitchat.common.ui.components.BodyText
import com.chitchat.common.ui.components.HightlightButton
import com.chitchat.common.ui.navigator.NavigatorViewModel
import com.chitchat.common.ui.navigator.Screen
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi


@OptIn(ExperimentalResourceApi::class)
@Composable
fun ConversationScreen(modifier: Modifier = Modifier,
                       platformEvent: StateFlow<PlatformEvent>,
                       navigator: NavigatorViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val viewModel = getViewModel("Conversation", viewModelFactory { ConversationViewModel() })
    val uiState = viewModel.uiState.collectAsState()
    viewModel.watch(platformEvent)
    val mode = remember { mutableStateOf("Portrait") }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) {
        BoxWithConstraints {
            mode.value = if (maxWidth < maxHeight) "Portrait" else "Landscape"
            uiState.value.displayMessage?.let {
                scope.launch {
                    snackbarHostState
                        .showSnackbar(
                            message = it,
                            duration = SnackbarDuration.Short
                        )
                    viewModel.messageShown()
                }
            }

            if (mode.value == "Portrait") {
                Column(
                    modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(color = Color.White),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    if (uiState.value.messages.isNotEmpty()) {
                        ConversationList(uiState)
                    } else {
                        EmptyConversation(viewModel, navigator)
                    }
                    ActionsLayoutHorizontal(uiState, viewModel, snackbarHostState)
                }
            } else {
                Row(
                    modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    Column(
                        Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .background(color = Color.White),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.Start
                    ) {
                        ConversationList(uiState)
                    }
                    ActionsLayoutVertical(uiState, viewModel, snackbarHostState)
                }
            }
        }
    }
}

@Composable
fun ColumnScope.ConversationList(uiState: State<ChatUiState>) {
    val lazyColumnListState = rememberLazyListState()
    val corroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = lazyColumnListState,
        modifier = Modifier
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
fun ColumnScope.EmptyConversation(
    viewModel: ConversationViewModel,
    navigator: NavigatorViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(MR.images.ic_chat),
            contentDescription = null
        )
        BodyText(
            modifier = Modifier
                .width(300.dp)
                .padding(top = 10.dp),
            text = stringResource(MR.strings.instructions)
        )
        HightlightButton(
            modifier = Modifier
                .width(200.dp)
                .padding(top = 20.dp),
            text = stringResource(MR.strings.start_conversation),
            onClick = { viewModel.onListenToggle() }
        )
    }
}



@Composable
fun ActionsLayoutVertical(uiState: State<ChatUiState>,
                          viewModel: ConversationViewModel,
                          snackbarHostState: SnackbarHostState) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(10.dp)
            .defaultMinSize(minWidth = 150.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally) {

        ActionIcons(uiState, viewModel, snackbarHostState)
    }
}

@Composable
fun ActionIcons(uiState: State<ChatUiState>,
                viewModel: ConversationViewModel,
                snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()

    IconTextButton(
        onClick = { if (uiState.value.messages.isNotEmpty()) viewModel.onShare() },
        icon = painterResource(MR.images.ic_share),
        text = stringResource(MR.strings.share),
        tintColor = if (uiState.value.messages.isNotEmpty())
            colorResource(MR.colors.primaryColor)
        else
            Color.Gray
    )


    val msgSummarize = stringResource(MR.strings.conversation_is_empty_summarize)
    IconTextButton(
        onClick = {
            if (uiState.value.messages.isNotEmpty()) {
                viewModel.onSummarize()
            } else {
                scope.launch {
                    snackbarHostState
                        .showSnackbar(
                            message = msgSummarize,
                            duration = SnackbarDuration.Short
                        )
                }
            }
        },
        icon = painterResource(MR.images.ic_summarize),
        text = stringResource(MR.strings.gpt_summarize),
        tintColor = if (uiState.value.messages.isNotEmpty())
            colorResource(MR.colors.primaryColor)
        else
            Color.Gray,
        isLoading = uiState.value.isGettingSummary
    )

    val msgAnswer = stringResource(MR.strings.conversation_is_empty)

    IconTextButton(
        onClick = {
            if (uiState.value.messages.isNotEmpty()) {
                viewModel.onGptAnswer()
            } else {
                scope.launch {
                    snackbarHostState
                        .showSnackbar(
                            message = msgAnswer,
                            duration = SnackbarDuration.Short
                        )
                }
            }
                  }, //if (uiState.value.messages.isNotEmpty()) viewModel.onGptAnswer() },
        icon = painterResource(MR.images.ic_gpt),
        text = stringResource(MR.strings.gpt_answer),
        tintColor = if (uiState.value.messages.isNotEmpty())
            colorResource(MR.colors.primaryColor)
        else
            Color.Gray,
        isLoading = uiState.value.isGettingAnswer
    )

    IconTextButton(
        onClick = { viewModel.onListenToggle() },

        icon = if (!uiState.value.isListening)
            painterResource(MR.images.ic_play)
        else
            painterResource(MR.images.ic_stop),

        text = if (!uiState.value.isListening)
            stringResource(MR.strings.start_interview)
        else
            stringResource(MR.strings.end_interview),

        tintColor = if (!uiState.value.isListening)
            colorResource(MR.colors.primaryColor)
        else
            Color.Red
    )
}

@Composable
fun IconTextButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: Painter,
    text: String,
    iconSize: Dp = 40.dp,
    tintColor: Color = colorResource(MR.colors.primaryColor),
    textSize: TextUnit = 14.sp,
    isLoading: Boolean = false
) {
    Column (
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(iconSize),
                color = colorResource(MR.colors.primaryColor)
            )
        } else {
            IconButton(
                onClick = { onClick() }
            ) {
                Icon(
                    modifier = Modifier.size(iconSize),
                    painter = icon,
                    tint = tintColor,
                    contentDescription = null
                )
            }
        }
        Text(text = text, fontSize = textSize)
    }
}

@Composable
fun ActionsLayoutHorizontal(uiState: State<ChatUiState>,
                            viewModel: ConversationViewModel,
                            snackbarHostState: SnackbarHostState) {
    Row(
        modifier= Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround) {
        ActionIcons(uiState, viewModel, snackbarHostState)
    }
}

@Composable
fun MessageCard(msg: ChatMessage) {
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