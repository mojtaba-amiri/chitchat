package com.chitchat.common.ui.navigator

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.chitchat.common.model.PlatformEvent
import com.chitchat.common.ui.screens.conversation.ConversationScreen
import com.chitchat.common.ui.screens.onboard.Onboard1
import com.chitchat.common.ui.screens.onboard.Onboard2
import kotlinx.coroutines.flow.StateFlow

@Composable
fun Navigator(modifier: Modifier = Modifier, navigatorVM: NavigatorViewModel,
              platformEvent: StateFlow<PlatformEvent>
) {
    val screen =  navigatorVM.currentScreen.collectAsState().value

    Crossfade(targetState = screen) { s ->
        when (s) {
            is Screen.Onboard1 -> {
                Onboard1(modifier = modifier, navigator = navigatorVM)
            }

            is Screen.Onboard2 -> {
                Onboard2(modifier = modifier, navigator = navigatorVM)
            }

            is Screen.Conversation -> {
                ConversationScreen(modifier = modifier,
                    platformEvent = platformEvent,
                    navigator = navigatorVM)
            }
        }
    }
}