package com.chitchat.common
expect fun getPlatformName(): String
expect fun getPlatformSpecificEvent(): PlatformSpecificEvent
expect class PlatformSpecificEvent {
    fun startListen()
    fun stopListen()
}