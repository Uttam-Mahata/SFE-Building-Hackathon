// This build file is specifically for JitPack to build only the client SDK module
// and avoid issues with the app module that might have different requirements

// Keep only the essential plugins
plugins {
    id("maven-publish")
}

// Define the subproject that we want JitPack to publish
subprojects { subproject ->
    // Only apply to the csfe module
    if (subproject.name == "csfe") {
        // Make sure maven-publish plugin is applied
        subproject.plugins.apply("maven-publish")
        
        // Ensure artifacts are properly named
        subproject.group = "com.github.Uttam-Mahata"
        subproject.version = "v1.0.0"
    }
}

// Skip the app module completely for JitPack builds
project(":app").afterEvaluate {
    tasks.configureEach {
        enabled = false
    }
}
