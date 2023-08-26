plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization") version Dependencies.kotlinSerializationVer
}

kotlin {
    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = Dependencies.iOS.version
        summary = Dependencies.iOS.summary
        homepage = Dependencies.iOS.homePage
        ios.deploymentTarget = Dependencies.iOS.deployTarget
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "MultiPlatformLibrary"
            isStatic = true
        }
        extraSpecAttributes["resources"] = "['src/commonMain/resources/**', 'src/iosMain/resources/**']"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation(Dependencies.kamel)
                implementation(Dependencies.ktorCore)
                implementation(Dependencies.ktorContentNegitiation)
                implementation(Dependencies.kotlinSerialization)
                implementation(Dependencies.kotlinDateTime)
                implementation(Dependencies.kotlinUuid)

                api(Dependencies.mokoMvvmCore)
                api(Dependencies.mokoMvvmFlow)
                api(Dependencies.mokoMvvmCompose)
                api(Dependencies.mokoMvvmComposeFlow)

                implementation(Dependencies.koinCore)
                implementation(Dependencies.koinTest)

            }
        }
        val androidMain by getting {
            dependencies {
                api(Dependencies.Android.compose)
                api(Dependencies.Android.appCompat)
                api(Dependencies.Android.coreKtx)
                api(Dependencies.Android.ktorCore)
                api(Dependencies.Android.koin)
                api(Dependencies.Android.koinCompose)
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
                implementation(Dependencies.iOS.ktorCore)
            }
        }
    }
}

android {
    compileSdk = Dependencies.Android.compileSdk
    namespace = Dependencies.Android.namespace

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = Dependencies.Android.minSdk
        targetSdk = Dependencies.Android.targetSdk
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        jvmToolchain(11)
    }
}
