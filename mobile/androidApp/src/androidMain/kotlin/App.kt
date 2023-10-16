package com.chitchat.android

import android.app.Application
import com.chitchat.common.di.appModule
import com.qonversion.android.sdk.BuildConfig
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionConfig
import com.qonversion.android.sdk.dto.QEnvironment
import com.qonversion.android.sdk.dto.QLaunchMode
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App: Application() {

    private val key = "ZIEh1Ko22thabeQStcvFoCqQ41OGh9Be"
    val androidModule = module {
        // Android specific dependencies
    }

    override fun onCreate() {
        super.onCreate()
        val qonversionConfig = QonversionConfig.Builder(
            this,
            key,
            QLaunchMode.SubscriptionManagement
        ).setEnvironment(
            if (BuildConfig.DEBUG) QEnvironment.Sandbox else QEnvironment.Production)
            .build()
        Qonversion.initialize(qonversionConfig)

        if (BuildConfig.DEBUG) {
            // init napier
            Napier.base(DebugAntilog())
        }

        startKoin {
            androidContext(this@App)
            // androidModule: refers to Android-specific dependencies
            // appModule: refers to Shared module dependencies
            modules(appModule() + androidModule)
        }
    }
}