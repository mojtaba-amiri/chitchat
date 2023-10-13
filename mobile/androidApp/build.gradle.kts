plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
}

kotlin {
    androidTarget()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(project(":voskmodel"))
                implementation("net.java.dev.jna:jna:5.13.0@aar")
                implementation("com.alphacephei:vosk-android:0.3.47@aar")
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.chitchat.android"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        applicationId = "com.chitchat.ChitChat"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = 106
        versionName = "1.06"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
    }
}
dependencies {
    implementation("com.android.billingclient:billing-ktx:6.0.1")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:2.9.9")
    implementation("com.android.billingclient:billing:6.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
//    implementation("com.google.android.gms:play-services-analytics-impl:18.0.3")
    implementation("io.qonversion.android.sdk:sdk:6.2.0")
}
