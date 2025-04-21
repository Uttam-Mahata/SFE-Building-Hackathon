plugins {
    id("com.android.library")
    id("maven-publish")
}

android {
    namespace = "com.app.sfe"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    
    // Security
    implementation(libs.security.crypto)
    
    // Biometric
    implementation("androidx.biometric:biometric:1.1.0")
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// Maven publishing configuration
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                
                groupId = "com.app.sfe"
                artifactId = "sfe-sdk"
                version = "1.0.0"
                
                pom {
                    name.set("SFE SDK")
                    description.set("Secure Financial Environment SDK for Android")
                    url.set("https://github.com/yourusername/sfe-sdk")
                    
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    
                    developers {
                        developer {
                            id.set("developer")
                            name.set("Developer Name")
                            email.set("developer@example.com")
                        }
                    }
                }
            }
        }
        
        repositories {
            maven {
                name = "local"
                url = uri("${layout.buildDirectory}/repo")
            }
            
            // Uncomment to publish to GitHub Packages
            /*
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/yourusername/sfe-sdk")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_API_KEY")
                }
            }
            */
        }
    }
}