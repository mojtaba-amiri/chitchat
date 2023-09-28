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
        var hasPremium: (() -> Boolean)? = null
        var shareAsTextFile: ((txt:String, name:String) -> Unit)? = null
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

    actual fun hasPremium() : Boolean {
        return hasPremium?.invoke() ?: false
    }

    actual fun shareAsTextFile(txt:String, name:String) {
        shareAsTextFile?.invoke(txt, name)
    }
}

@Composable fun MainView(message: StateFlow<PlatformEvent>) = App(message)

fun messageReceived(message: String) = onMessageReceived(message)

fun errorOnRecognizer(e: Exception) = onRecognizerError(e)

fun timeoutOnRecognizer() = onRecognizerTimeOut()

