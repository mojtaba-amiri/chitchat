package com.chitchat.android

import android.util.Log
import androidx.lifecycle.ViewModel
import com.chitchat.common.model.EventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.chitchat.common.model.PlatformEvent
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.dto.QonversionError
import com.qonversion.android.sdk.dto.entitlements.QEntitlement
import com.qonversion.android.sdk.dto.offerings.QOffering
import com.qonversion.android.sdk.dto.offerings.QOfferings
import com.qonversion.android.sdk.listeners.QonversionEntitlementsCallback
import com.qonversion.android.sdk.listeners.QonversionOfferingsCallback

class EventViewModel: ViewModel() {

    private val _event: MutableStateFlow<PlatformEvent> = MutableStateFlow(PlatformEvent())
    val event = _event.asStateFlow()

    var offerings : MutableList<QOffering> = mutableListOf()
    var hasPremium = false

    init {
        loadOfferings()
        updatePermissions()
    }

    fun onEvent(msg: String = "", e: Exception? = null,
                eType: String = "Recognizer",
                timeOut: Boolean = false) {
        Log.e("EVENT", "EVENT: $eType   $msg")
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

    fun updatePermissions() {
        Qonversion.shared.checkEntitlements(object : QonversionEntitlementsCallback {
            override fun onError(error: QonversionError) {
                _event.tryEmit(
                    PlatformEvent(
                        message ="error",
                        eType = "PremiumAccess"
                    )
                )
            }

            override fun onSuccess(entitlements: Map<String, QEntitlement>) {
                hasPremium = entitlements["Premium"]?.isActive == true
                if (hasPremium) {
                    _event.tryEmit(
                        PlatformEvent(
                            message ="hasPremium",
                            eType = "PremiumAccess"
                        )
                    )
                }
            }
        })
    }

}