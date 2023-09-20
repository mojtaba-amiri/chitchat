package com.chitchat.android

import androidx.lifecycle.ViewModel
import com.chitchat.common.model.EventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.chitchat.common.model.PlatformEvent

class EventViewModel: ViewModel() {

    private val _event: MutableStateFlow<PlatformEvent> = MutableStateFlow(PlatformEvent())
    val event = _event.asStateFlow()

    fun onEvent(msg: String = "", e: Exception? = null,
                eType: String = "Recognizer",
                timeOut: Boolean = false) {
        _event.tryEmit(PlatformEvent(msg, e, eType = eType, timeOut))
    }

}