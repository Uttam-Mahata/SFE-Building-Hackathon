// This build file is specifically for JitPack to build only the client SDK module
// and avoid issues with the app module that might have different requirements

// Keep only the essential plugins
plugins {
    id("maven-publish")
}

// Set project-wide Java compatibility
allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "21"
        }
    }
    
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_21.toString()
        targetCompatibility = JavaVersion.VERSION_21.toString()
    }
}

// Define the subproject that we want JitPack to publish
subprojects { subproject ->
    // Only apply to the csfe module
    if (subproject.name == "csfe") {
        // Make sure maven-publish plugin is applied
        subproject.plugins.apply("maven-publish")
        
        // Ensure artifacts are properly named
        subproject.group = "com.github.Uttam-Mahata"
        subproject.version = "v1.0.1"
    }
}

// Skip the app module completely for JitPack builds
gradle.projectsEvaluated {
    project(":app").tasks.configureEach {
        enabled = false
    }
}
