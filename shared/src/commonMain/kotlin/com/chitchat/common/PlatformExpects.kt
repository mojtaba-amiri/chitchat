package com.chitchat.common
expect fun getPlatformName(): String
expect fun getPlatformSpecificEvent(): PlatformSpecificEvent
expect class PlatformSpecificEvent {
    fun startListen()
    fun stopListen()
    fun startPurchase()
    fun startBillingConnection()
    fun shareAsTextFile(txt:String, name:String)
}