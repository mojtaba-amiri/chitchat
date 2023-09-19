package com.chitchat.common

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chitchat.common.model.PlatformEvent
import com.chitchat.common.ui.navigator.Navigator
import com.chitchat.common.ui.navigator.NavigatorViewModel
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import kotlinx.coroutines.flow.StateFlow

var newEvent: PlatformEvent = PlatformEvent()

@Composable
fun App(newEvent: StateFlow<PlatformEvent>) {
    MaterialTheme {
        val navigatorViewModel = getViewModel("Navigator", viewModelFactory { NavigatorViewModel() })
        navigatorViewModel.watch(newEvent)
        Navigator(modifier = Modifier,
            navigatorVM = navigatorViewModel,
            platformEvent = newEvent)
//        val viewModel = getViewModel("Chat", viewModelFactory { ChatViewModel() })
//        viewModel.watch(newEvent)
//        MainPage(Modifier, viewModel)
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