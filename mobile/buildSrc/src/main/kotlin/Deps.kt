//
object Deps {
    const val kamel = "media.kamel:kamel-image:0.7.1"

    const val napier = "io.github.aakira:napier:2.6.1"

    const val ktorVersion = "2.3.4"
    const val ktorCore = "io.ktor:ktor-client-core:$ktorVersion"
    const val ktorContentCIO = "io.ktor:ktor-client-cio:$ktorVersion"
    const val ktorContentNegitiation = "io.ktor:ktor-client-content-negotiation:$ktorVersion"
    const val ktorSerialization = "io.ktor:ktor-serialization-kotlinx-json:$ktorVersion"
    const val ktorLogging = "io.ktor:ktor-client-logging:$ktorVersion"

    const val kotlinxJson = "io.ktor:ktor-serialization-kotlinx-json:$ktorVersion"

    const val kotlinSerializationVer = "1.6.0"
    const val kotlinSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinSerializationVer}"

    const val kotlinDateTime = "org.jetbrains.kotlinx:kotlinx-datetime:0.4.1"
    const val kotlinUuid = "app.softwork:kotlinx-uuid-core:0.0.21"

    private const val mokoVersion = "0.16.1"
    const val mokoMvvmCore = "dev.icerock.moko:mvvm-core:$mokoVersion"
    const val mokoMvvmFlow = "dev.icerock.moko:mvvm-flow:$mokoVersion"
    const val mokoMvvmCompose = "dev.icerock.moko:mvvm-compose:$mokoVersion"
    const val mokoMvvmComposeFlow = "dev.icerock.moko:mvvm-flow-compose:$mokoVersion"

    private const val mokoResourcesVersion = "0.23.0"
    const val mokoResources = "dev.icerock.moko:resources:$mokoResourcesVersion"
    const val mokoResourcesCompose = "dev.icerock.moko:resources-compose:$mokoResourcesVersion"

    private const val voyagerVersion = "1.0.0-rc05"
    const val voyager = "cafe.adriel.voyager:voyager-navigator:$voyagerVersion"
    const val voyagerNav = "cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion"
    const val voyagerTransitions = "cafe.adriel.voyager:voyager-transitions:$voyagerVersion"

    const val koinVersion = "3.4.3"
    const val koinCore = "io.insert-koin:koin-core:$koinVersion"
    const val koinTest = "io.insert-koin:koin-test:$koinVersion"

    private const val firebaseKotlinVersion = "1.9.0"
    const val firebaseAuth = "dev.gitlive:firebase-auth:$firebaseKotlinVersion"

    private const val settingsVersion = "1.1.0"
    const val settings = "com.russhwolf:multiplatform-settings-no-arg:$settingsVersion"
//
    object Android {
        const val minSdk = 24
        const val targetSdk = 33
        const val compileSdk = 33
        const val namespace = "com.chitchat.common"
        const val coreKtx = "androidx.core:core-ktx:1.9.0"
        const val appCompat = "androidx.appcompat:appcompat:1.6.1"
        const val compose = "androidx.activity:activity-compose:1.6.1"
        const val composeUI = "androidx.compose.ui:ui:1.1.0"
        const val composeMaterial = "androidx.compose.material:material:1.5.0-rc01"
        const val ktorCore = "io.ktor:ktor-client-android:$ktorVersion"

        const val koin = "io.insert-koin:koin-android:3.4.3"
        const val koinCompose = "io.insert-koin:koin-androidx-compose:3.4.6"
    }

    object iOS {
        const val version = "1.0.0"
        const val summary = "Some description for the Shared Module"
        const val homePage = "Link to the Shared Module homepage"
        const val deployTarget = "14.1"

        const val ktorCore = "io.ktor:ktor-client-darwin:$ktorVersion"
    }
}