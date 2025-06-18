// For JitPack publishing
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Keep empty for now, JitPack will handle the build
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
