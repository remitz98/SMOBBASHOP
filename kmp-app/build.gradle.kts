plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Ktor HTTP Client for Cloudflare API
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            // Coil 3 Image Loading for Compose
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
            implementation("androidx.core:core-ktx:1.13.1")
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.cio)
            }
        }
    }
}

android {
    namespace = "org.smobba.store"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.smobba.store"
        minSdk = 24
        targetSdk = 35
        versionCode = 19
        versionName = "1.0.19"
    }

    signingConfigs {
        create("release") {
            storeFile = file("keystore/smobba-release.jks")
            storePassword = "smobba123456"
            keyAlias = "smobba"
            keyPassword = "smobba123456"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.smobba.store.MainKt"
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe
            )
            packageName = "SMO BBA SHOP"
            packageVersion = "1.0.19"
            description = "SMO BBA SHOP Official Store Application"
            vendor = "SMO BBA UBRU"
            windows {
                menuGroup = "SMO BBA SHOP"
                shortcut = true
                dirChooser = true
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }
            macOS {
                bundleID = "org.smobba.store"
                iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
            }
        }
    }
}
