plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.h0tk3y.incomplete.producer")
    id("maven-publish")
}

group = "com.h0tk3y.incomplete"
version = "1.0"

repositories {
    google()
    mavenCentral()
}

configure<PublishingExtension> {
    repositories {
        maven("$rootDir/../build/repo")
    }
}

android {
    compileSdkVersion = "android-30"
}

kotlin {
    android {
        publishAllLibraryVariants()
    }
    // These targets are provided externally:
    jvm { }
    js(IR) { }
}

incomplete {
    incompleteTarget(kotlin.targets.getByName("jvm"))
    incompleteTarget(kotlin.targets.getByName("js"))
}
