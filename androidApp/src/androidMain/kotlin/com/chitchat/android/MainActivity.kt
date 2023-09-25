package com.chitchat.android

import android.Manifest
import android.R.attr.text
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
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import com.chitchat.common.MainView
import com.chitchat.common.PlatformSpecificEvent
import com.chitchat.common.errorOnRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.IOException


class MainActivity : AppCompatActivity(), RecognitionListener, PurchasesUpdatedListener {

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private val viewModel = EventViewModel()
    private var permissionToRecordAccepted = false

    companion object {
        const val SUBSCRIPTION_NAME = "one_month_subscription"
    }

    private val billingClient by lazy {
        BillingClient.newBuilder(applicationContext)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }

    private var productDetailsResult : ProductDetailsResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PlatformSpecificEvent.startRecognizer = { startListen() }
        PlatformSpecificEvent.stopRecognizer = { stopListen() }
        PlatformSpecificEvent.startBillingConnection = { startBillingConnection() }
        PlatformSpecificEvent.startPurchase = { startPurchase() }
        PlatformSpecificEvent.shareAsTextFile =  { txt, name -> shareAsTextFile(txt, name) }

        initModel()
        setContent {
            MainView(viewModel.event)
        }
    }

    override fun onResume() {
        super.onResume()
        startBillingConnection()
        checkPermission()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.RECORD_AUDIO), 1001)
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
            Toast.makeText(this, "This app needs audio recording permission. Please grant this permission.", Toast.LENGTH_LONG).show()
        }
    }


    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    lifecycleScope.launch(Dispatchers.IO) {
                        queryPurchases()
                    }
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                viewModel.onEvent(msg = "Disconnected", eType = "Purchase")
            }
        })
    }

    suspend fun queryPurchases() {
        val productList = ArrayList<QueryProductDetailsParams.Product>()
        productList.add(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SUBSCRIPTION_NAME)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        // leverage queryProductDetails Kotlin extension function
        productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }
    }

    private fun startPurchase() {
        if (productDetailsResult?.billingResult?.responseCode == BillingResponseCode.ITEM_ALREADY_OWNED) {
            viewModel.onEvent(msg = "AlreadyOwned", eType = "Purchase")
            return
        }
        productDetailsResult?.productDetailsList?.firstOrNull()?.let { productDetail ->
            productDetail.subscriptionOfferDetails?.firstOrNull()?.offerToken?.let { token ->
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                        .setProductDetails(productDetail)
                        // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                        // for a list of offers that are available to the user
                        .setOfferToken(token)
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                val billingResult = billingClient.launchBillingFlow(this@MainActivity, billingFlowParams)
                if (billingResult.responseCode != BillingResponseCode.OK) {
                    if (billingResult.responseCode != BillingResponseCode.ITEM_ALREADY_OWNED) {
                        viewModel.onEvent(msg = "AlreadyOwned", eType = "Purchase")
                    } else {
                        viewModel.onEvent(msg = "BillingNotLaunched", eType = "Purchase")
                    }
                } else {
                    // Billing is launched. The result will be delivered to "onPurchasesUpdated"
                }
            }
        } ?: {
            viewModel.onEvent(msg = "BillingAPINotReady", eType = "Purchase")
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                viewModel.onEvent(msg = "OK", eType = "Purchase")
            }
        } else if (billingResult.responseCode == BillingResponseCode.USER_CANCELED) {
            viewModel.onEvent(msg = "UserCanceled", eType = "Purchase")
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            viewModel.onEvent(msg = "Error:${billingResult.responseCode}", eType = "Purchase")
        }
        if (billingResult.responseCode == BillingResponseCode.ITEM_ALREADY_OWNED) {
            viewModel.onEvent(msg = "AlreadyOwned", eType = "Purchase")
            return
        }
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

