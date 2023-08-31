package com.chitchat

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.PlatformEvent

class EventViewModel: ViewModel() {

    private val _event: MutableStateFlow<PlatformEvent> = MutableStateFlow(PlatformEvent())
    val event = _event.asStateFlow()

    fun onEvent(msg: String = "", e: Exception? = null, timeOut: Boolean = false) {
        _event.tryEmit(PlatformEvent(msg, e, timeOut))
    }

}