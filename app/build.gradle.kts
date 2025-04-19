plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.app.sfe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.app.sfe"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    
    // Lifecycle components
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.gson)
    
    // Security
    implementation(libs.security.crypto)
    
    // Room database
    implementation(libs.room.runtime)
    //implementation(libs.androidx.security.crypto)
    annotationProcessor(libs.room.compiler)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}