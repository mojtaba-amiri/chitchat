package com.chitchat.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chitchat.common.MainView
import com.chitchat.common.PlatformSpecificEvent
import com.chitchat.common.errorOnRecognizer
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.dto.QonversionError
import com.qonversion.android.sdk.dto.entitlements.QEntitlement
import com.qonversion.android.sdk.listeners.QonversionEntitlementsCallback
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.IOException

class MainActivity : AppCompatActivity(), RecognitionListener {

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private val viewModel = EventViewModel()
    private var permissionToRecordAccepted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PlatformSpecificEvent.startRecognizer = { startListen() }
        PlatformSpecificEvent.stopRecognizer = { stopListen() }
        PlatformSpecificEvent.hasPremium = { viewModel.hasPremium }
        PlatformSpecificEvent.startPurchase = { startPurchase() }
        PlatformSpecificEvent.shareAsTextFile =  { txt, name -> shareAsTextFile(txt, name) }

        initModel()
        setContent {
            MainView(viewModel.event)
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf( Manifest.permission.RECORD_AUDIO),
                1001)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == 1001) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) {
            Toast.makeText(this, R.string.permission_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun startPurchase() {
        Qonversion.shared.purchase(
            context = this,
            product = viewModel.offerings.firstOrNull()?.products?.firstOrNull() ?: return,
            callback = object : QonversionEntitlementsCallback {
                override fun onError(error: QonversionError) {
                    viewModel.onEvent("Error", eType = "Purchase")
                }

                override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                    viewModel.onEvent("Success", eType = "Purchase")
                    viewModel.updatePermissions()
                }
            })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewModel.onEvent(msg = "Landscape", eType = "Orientation")
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            viewModel.onEvent(msg = "Portrait", eType = "Orientation")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechService?.stop()
        speechService?.shutdown()
    }

    private fun initModel() {
        StorageService.unpack(this, "model-en-us", "com/chitchat/common/model",
            { model: Model? ->
                this.model = model
                Log.e("VoskModel","Model Loaded" )
            }
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

    private fun shareAsTextFile(txt: String, name: String) {
        try {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_TEXT, txt)
            startActivity(sharingIntent)
//            val fileName = "${name}.txt"
//            this.write(txt, fileName)
//            val file = File(filesDir, fileName)
//            val fileUri: Uri? =
//                FileProvider.getUriForFile(
//                    this@MainActivity,
//                    "com.chitchat.android.fileprovider",
//                    file
//                )
//            fileUri?.let {
//                val intent = Intent()
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
////                intent.setDataAndType(fileUri, contentResolver.getType(fileUri))
////                intent.type = "text/*"
//                startActivity(intent)
//            }
        } catch (e: IllegalArgumentException) {
                Log.e("File Selector",
                    "The selected file can't be shared: $name")
            Toast.makeText(this, "Error Sharing file", Toast.LENGTH_LONG).show()
        }
        catch (e: Exception) {
            Toast.makeText(this, "Error Sharing file", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPartialResult(hypothesis: String?) {
//        Log.d("MainActivity", "$hypothesis")
        hypothesis?.let { viewModel.onEvent(it, eType = "Recognizer") }
    }

    override fun onResult(hypothesis: String?) {
        hypothesis?.let { viewModel.onEvent(it) }
    }

    override fun onFinalResult(hypothesis: String?) {
        hypothesis?.let { viewModel.onEvent(it) }
    }

    override fun onError(exception: Exception?) {
        exception?.let { viewModel.onEvent(e = it) }
    }

    override fun onTimeout() {
        viewModel.onEvent(timeOut = true)
    }

    override fun onBackPressed() {
        viewModel.onEvent(msg = "BackPress", eType = "UserEvent")
    }
}

