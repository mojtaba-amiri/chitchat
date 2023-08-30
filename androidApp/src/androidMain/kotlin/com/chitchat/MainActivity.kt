package com.chitchat

import MainView
import PlatformSpecificEvent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import errorOnRecognizer
import messageReceived
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import timeoutOnRecognizer
import java.io.IOException
import java.lang.Exception

class MainActivity : AppCompatActivity(), RecognitionListener {

    private var model: Model? = null
    private var speechService: SpeechService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PlatformSpecificEvent.startRecognizer = { startListen() }
        PlatformSpecificEvent.stopRecognizer = { stopListen() }

        initModel()
        setContent {
            MainView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechService?.stop()
        speechService?.shutdown()
    }

    private fun initModel() {
        StorageService.unpack(this, "model-en-us", "model",
            { model: Model? -> this.model = model }
        ) { exception: IOException ->
            errorOnRecognizer(exception)
        }
    }

    private fun startListen() {
        if (speechService != null) {
            speechService?.stop()
            speechService = null
        } else {
            try {
                val rec = Recognizer(model, 16000.0f)
                speechService = SpeechService(rec, 16000.0f)
                speechService?.startListening(this)
            } catch (e: IOException) {
                errorOnRecognizer(e)
            }
        }
    }

    private fun stopListen() {
        speechService?.stop()
        speechService = null
    }

    override fun onPartialResult(hypothesis: String?) {
        Log.d("MainActivity", "$hypothesis")
        hypothesis?.let { messageReceived(it) }
    }

    override fun onResult(hypothesis: String?) {
        hypothesis?.let { messageReceived(it) }
    }

    override fun onFinalResult(hypothesis: String?) {
        hypothesis?.let { messageReceived(it) }
    }

    override fun onError(exception: Exception?) {
        exception?.let { errorOnRecognizer(it) }
    }

    override fun onTimeout() {
        timeoutOnRecognizer()
    }
}

