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
    actual fun hasPremium(): Boolean {
        return hasPremium?.invoke() ?: false
    }

    actual fun shareAsTextFile(txt:String, name:String) {
        shareAsTextFile?.invoke(txt, name)
    }
}

fun MainViewController(event: StateFlow<PlatformEvent>) = ComposeUIViewController { App(event) }