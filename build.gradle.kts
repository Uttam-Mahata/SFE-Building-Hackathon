// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("maven-publish")
}

// Define version information for the entire project
ext {
    val libraryVersion = "1.0.0"
    val libraryName = "sfe-sdk"
    val libraryGroup = "com.app.sfe"
}