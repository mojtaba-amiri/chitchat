package com.chitchat.common

import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.flow.StateFlow
import com.chitchat.common.model.PlatformEvent

actual fun getPlatformName(): String = "iOS"

actual fun getPlatformSpecificEvent() = PlatformSpecificEvent()

actual class PlatformSpecificEvent{
    companion object {
        var startRecognizer: (() -> Unit)? = null
        var stopRecognizer: (() -> Unit)? = null
        var startPurchase: (() -> Unit)? = null
        var startBillingConnection: (() -> Unit)? = null
    }
    actual fun startListen() {
        startRecognizer?.invoke()
    }
    actual fun stopListen() {
        stopRecognizer?.invoke()
    }
    actual fun startPurchase() {
        startPurchase?.invoke()
    }
    actual fun startBillingConnection() {
        startBillingConnection?.invoke()
    }
}

fun MainViewController(event: StateFlow<PlatformEvent>) = ComposeUIViewController { App(event) }