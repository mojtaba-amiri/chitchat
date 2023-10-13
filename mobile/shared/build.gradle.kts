plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization") version "1.9.0" //Dependencies.kotlinSerializationVer
    id("dev.icerock.mobile.multiplatform-resources")
//    kotlin("plugin.serialization") version Dependencies.kotlinSerializationVer
}

multiplatformResources {
    multiplatformResourcesPackage = "com.chitchat.common" // required
//    multiplatformResourcesClassName = "SharedRes" // optional, default MR
//    multiplatformResourcesVisibility = MRVisibility.Internal // optional, default Public
    iosBaseLocalizationRegion = "en" // optional, default "en"
    multiplatformResourcesSourceSet = "commonMain"  // optional, default "commonMain"
}

kotlin {
    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = Deps.iOS.version
        summary = Deps.iOS.summary
        homepage = Deps.iOS.homePage
        ios.deploymentTarget = Deps.iOS.deployTarget
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "MultiPlatformLibrary"
            isStatic = true
            export("dev.icerock.moko:resources:0.23.0")
            export("dev.icerock.moko:graphics:0.9.0") // toUIColor here
        }
        extraSpecAttributes["resources"] = "['src/commonMain/resources/**', 'src/iosMain/resources/**']"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {

                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                // Ktor
                implementation(Deps.ktorCore)
                implementation(Deps.ktorContentCIO)
                implementation(Deps.ktorContentNegitiation)
                implementation(Deps.ktorSerialization)

                // Serializations
                implementation(Deps.kotlinSerialization)
                implementation(Deps.kotlinDateTime)
                implementation(Deps.kotlinUuid)

                // Image Loading
                implementation(Deps.kamel)

                // MultiPlatform Resources
                api(Deps.mokoResources)
                api(Deps.mokoResourcesCompose)

                // MVVM
                api(Deps.mokoMvvmCore)
                api(Deps.mokoMvvmFlow)
                api(Deps.mokoMvvmCompose)
                api(Deps.mokoMvvmComposeFlow)

                // Logging
                implementation(Deps.kermit)

                // Navigation
                implementation(Deps.voyager)
                // BottomSheetNavigator
                implementation(Deps.voyagerNav)
                // Navigation Transitions
                implementation(Deps.voyagerTransitions)

                // Koin DI
                implementation(Deps.koinCore)
                implementation(Deps.koinTest)

                // KeyValue Store
                implementation(Deps.settings)

            }
        }
        val androidMain by getting {
            dependencies {
                api(Deps.Android.compose)
                api(Deps.Android.appCompat)
                api(Deps.Android.coreKtx)
                api(Deps.Android.ktorCore)
                api(Deps.Android.koin)
                api(Deps.Android.koinCompose)

                api("net.java.dev.jna:jna:5.13.0@aar")
                api("com.alphacephei:vosk-android:0.3.47@aar")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation(Deps.iOS.ktorCore)
            }
        }
    }
}

android {
    compileSdk = Deps.Android.compileSdk
    namespace = Deps.Android.namespace

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = Deps.Android.minSdk
        targetSdk = Deps.Android.targetSdk
    }
    dependencies {
//        implementation("com.android.billingclient:billing:6.0.1")
        implementation("com.android.billingclient:billing-ktx:6.0.1")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
    }
}
