package com.chitchat.android

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.chitchat.common.model.PlatformEvent
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.dto.QUser
import com.qonversion.android.sdk.dto.QonversionError
import com.qonversion.android.sdk.dto.entitlements.QEntitlement
import com.qonversion.android.sdk.dto.offerings.QOffering
import com.qonversion.android.sdk.dto.offerings.QOfferings
import com.qonversion.android.sdk.listeners.QonversionEntitlementsCallback
import com.qonversion.android.sdk.listeners.QonversionOfferingsCallback
import com.qonversion.android.sdk.listeners.QonversionUserCallback
import okhttp3.WebSocket

class EventViewModel: ViewModel() {
    private var socket: WebSocket? = null

    private val _event: MutableStateFlow<PlatformEvent> = MutableStateFlow(PlatformEvent())
    val event = _event.asStateFlow()

    var offerings : MutableList<QOffering> = mutableListOf()
    var hasPremium = false
    var userId: String? = null

    init {
        loadUser()
        loadOfferings()
        updatePermissions()
    }

    fun onEvent(msg: String = "", e: Exception? = null,
                eType: String = "Recognizer",
                timeOut: Boolean = false) {
//        Log.e("EVENT", "EVENT: $eType   $msg")
        _event.tryEmit(PlatformEvent(msg, e, eType = eType, timeOut))
    }

    fun loadOfferings() {
        Qonversion.shared.offerings(object : QonversionOfferingsCallback {
            override fun onError(error: QonversionError) {
                _event.tryEmit(
                    PlatformEvent(
                        message ="error",
                        eType = "Offerings"
                    )
                )
            }

            override fun onSuccess(offerings: QOfferings) {
                this@EventViewModel.offerings = offerings.availableOfferings.toMutableList()
                _event.tryEmit(
                    PlatformEvent(
                        message = "updated",
                        eType = "Offerings"
                    )
                )
            }
        })
    }

//    fun socket() {
//        val httpClient = OkHttpClient()
//        val request = Request.Builder()
//            .url("ws://echo.websocket.org")
//            .build()
//        val webSocket = httpClient.newWebSocket(request, this)
//        //webSocket.
//        httpClient.dispatcher.executorService.shutdown()
//    }

    fun updatePermissions() {
        Qonversion.shared.checkEntitlements(object : QonversionEntitlementsCallback {
            override fun onError(error: QonversionError) {
                _event.tryEmit(
                    PlatformEvent(
                        message ="error",
                        error = Exception("Not Premium"),
                        eType = "PremiumAccess"
                    )
                )
            }

            override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                confirmPermission(entitlements["Premium"]?.isActive == true)
            }
        })
    }

    fun confirmPermission(value: Boolean) {
        hasPremium = value
        if (hasPremium) {
            _event.tryEmit(
                PlatformEvent(
                    message ="$userId",
                    eType = "PremiumAccess"
                )
            )
        }
    }

    private fun loadUser() {
        Qonversion.shared.userInfo(object : QonversionUserCallback {
            override fun onSuccess(user: QUser) {
                userId = user.qonversionId
                _event.tryEmit(
                    PlatformEvent(
                        message ="$userId",
                        eType = "UserId"
                    )
                )
                // use user.qonversionId
            }

            override fun onError(error: QonversionError) {
                // handle error here
            }
        })
    }

}