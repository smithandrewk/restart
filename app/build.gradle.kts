plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.delta.restart"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.delta.restart"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Google Play services wearable
    implementation(libs.play.services.wearable)

    // Compose BOM (Bill of Materials) to manage versions of Compose libraries
    implementation(platform(libs.compose.bom))

    // Core Compose libraries
    implementation(libs.ui)                              // Compose UI
    implementation(libs.ui.tooling.preview)              // Compose UI Tooling Preview
    implementation(libs.compose.material)                // Material Design components in Compose
    implementation(libs.compose.foundation)              // Foundation components for Compose

    // Wear OS-specific Compose libraries
    implementation(libs.wear.compose.foundation)         // Compose Foundation for Wear OS
    implementation(libs.wear.compose.material)           // Material Design for Wear OS
    implementation(libs.wear.compose.navigation)         // Navigation in Wear OS Compose

    // Activity and Splash Screen in Compose
    implementation(libs.activity.compose)                // Activity support for Compose
    implementation(libs.core.splashscreen)               // Core Splash Screen support

    // Android Test Dependencies
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)       // Compose testing dependencies

    // Debug dependencies for Compose
    debugImplementation(libs.ui.tooling)                 // Tooling support
    debugImplementation(libs.ui.test.manifest)           // Manifest testing support
}