package com.chitchat.android.com.chitchat.android

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class SocketListener (
    val onConnected: (ws: WebSocket) -> Unit,
    val onDisconnected: (ws: WebSocket) -> Unit
) : WebSocketListener() {
    private val NORMAL_CLOSURE_STATUS = 1000

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        onConnected(webSocket)
    }

    override fun onMessage(webSocket: WebSocket, text: String){
        super.onMessage(webSocket, text)
        outputData("Receiving $text")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        outputData("Receiving bytes : " + bytes.hex())

    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        outputData("$code $reason")
    }

    private fun outputData(outputString: String) {

    }
}