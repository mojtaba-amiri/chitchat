package com.chitchat.common.ui.navigator

import com.chitchat.common.model.EventType
import com.chitchat.common.model.PlatformEvent
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(val id: Int, val safeArgs: Map<String, String> = mapOf()) {
    data object Conversation: Screen(id = 0)
    data object Onboard1: Screen(id = 1)
    data object Onboard2: Screen(id = 2)
}


class NavigatorViewModel: ViewModel() {
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Onboard1)
    val currentScreen = _currentScreen.asStateFlow()
    private val screenStack = mutableListOf<Screen>(Screen.Onboard1)

    fun watch(msg: StateFlow<PlatformEvent>) = this.viewModelScope.launch {
        msg.collect { onNewMessage(it) }
    }

    private fun onNewMessage(msg: PlatformEvent) {
        this.viewModelScope.launch {
            if (msg.eType == "UserEvent") {
                when {
                    msg.message.startsWith("BackPress") -> {
                        pop()
                    }
                }
            }
        }

        // write code to handle event passed from platform
        //        this.viewModelScope.launch {
        //            when {
        //                event.message.isNotEmpty() -> {}
        //            }
        //        }
    }

    fun navigate(screen: Screen, addToStack: Boolean = true, arg: Map<String, String> = mapOf()) {
        _currentScreen.update { screen }
        if (addToStack) screenStack.add(screen)
    }

    fun pop() {
        if (screenStack.size > 1)  {
            screenStack.removeLast()
            navigate(screen = screenStack.last(), addToStack = false)
        }
    }

    fun popAll() {
        while (screenStack.size > 1)  {
            screenStack.removeLast()
            navigate(screenStack.last())
        }
    }
}