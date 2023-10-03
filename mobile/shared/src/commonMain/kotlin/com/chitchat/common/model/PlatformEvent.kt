package com.chitchat.common.model

import kotlinx.datetime.Clock

sealed class EventType {
    data object Recognizer: EventType()
    data object UserEvent: EventType()

    data object GeneralEvent: EventType()
}

data class PlatformEvent(
    val message: String = "",
    val error: Exception? = null,
    val eType: String = "",
    val timeout: Boolean = false,
    val id: Long = Clock.System.now().toEpochMilliseconds(),
)