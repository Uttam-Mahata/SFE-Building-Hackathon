plugins {
    `java-library`
    // kotlin("jvm") // Removed explicit kotlin("jvm") plugin - relying on global Kotlin version
    id("maven-publish")
}

// Repositories are defined in settings.gradle.kts
// repositories {
//     mavenCentral()
// }

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    // Add other dependencies here
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["java"]) // Use 'java' component for a Kotlin/Java library
                groupId = "com.github.Uttam-Mahata.SFE-Building-Hackathon" // Match existing csfe
                artifactId = "backend-sdk" // As per BACKEND-SDK-README.md
                version = "v1.0.0"       // Placeholder version
            }
        }
    }
}
