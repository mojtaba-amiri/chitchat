package com.chitchat.common

import androidx.compose.runtime.Composable
import com.chitchat.common.model.PlatformEvent
import kotlinx.coroutines.flow.StateFlow

actual fun getPlatformName(): String = "Android"

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

@Composable fun MainView(message: StateFlow<PlatformEvent>) = App(message)

fun messageReceived(message: String) = onMessageReceived(message)

fun errorOnRecognizer(e: Exception) = onRecognizerError(e)

fun timeoutOnRecognizer() = onRecognizerTimeOut()

