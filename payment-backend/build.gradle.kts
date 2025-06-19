plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
}

// Group and version are often set for Spring Boot projects,
// though not strictly necessary for the build to pass for this subtask.
// group = "com.sfe"
// version = "0.0.1-SNAPSHOT" // Example version

java {
    sourceCompatibility = JavaVersion.VERSION_17 // Spring Boot 3.x typically requires Java 17+
    targetCompatibility = JavaVersion.VERSION_17
}

// Repositories are managed in settings.gradle.kts

dependencies {
    implementation(project(":backend-sdk")) // Added dependency on backend-sdk
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // Added JPA starter
    implementation(kotlin("stdlib-jdk8")) // Or just kotlin("stdlib") if JDK 8 features aren't specifically needed
    // For Kotlin + Spring, ensure all necessary Kotlin dependencies are included
    implementation("org.jetbrains.kotlin:kotlin-reflect") // Often needed for Spring with Kotlin, uncommenting
    runtimeOnly("com.h2database:h2") // Added H2 database

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict") // For Spring nullability annotations
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("com.sfe.paymentbackend.PaymentBackendApplicationKt") // Correct Main class name
}

// Optional: If you need to ensure compatibility with specific Spring versions for dependencies
dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}
