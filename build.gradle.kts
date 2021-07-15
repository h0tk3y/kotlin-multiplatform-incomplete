plugins {
    kotlin("multiplatform").apply(false)
    kotlin("jvm").apply(false)
    id("com.android.library").apply(false)
}

allprojects {
    group = "com.h0tk3y"
    version = "1.0"

    repositories {
        google()
        mavenCentral()
    }

    pluginManager.withPlugin("maven-publish") {
        configure<PublishingExtension> {
            repositories {
                maven("$rootDir/build/repo")
            }
        }
    }
}