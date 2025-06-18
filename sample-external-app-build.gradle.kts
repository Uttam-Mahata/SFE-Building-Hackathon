plugins {
    id("com.android.application") version "8.10.0"
    id("org.jetbrains.kotlin.android") version "2.0.0"
}

android {
    namespace = "com.example.sfedemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sfedemo"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}

repositories {
    google()
    mavenCentral()
    // Add JitPack repository
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // Import the SFE Client SDK from JitPack
    implementation("com.github.Uttam-Mahata:SFE-Building-Hackathon:v1.0.1")

    // Standard Android dependencies
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    // Other dependencies your app needs...
}
