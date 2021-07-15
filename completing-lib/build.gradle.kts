plugins {
    kotlin("multiplatform")
    `maven-publish`
    id("com.h0tk3y.incomplete.completing")
}

group = "com.h0tk3y.completing"
version = "1.0"

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven("$rootDir/../build/repo")
}

kotlin {
    jvm { }
    js(IR) {
        browser()
        nodejs()
    }
}

completing {
    completingMultiplatformLibrary(
        "com.h0tk3y.incomplete:incomplete-lib:1.0",
        files("$rootDir/../incomplete-lib/src/commonMain/kotlin"),
        { kotlinTarget -> kotlinTarget.name }
    )
}

publishing {
    repositories {
        maven("$rootDir/../build/repo")
    }
}