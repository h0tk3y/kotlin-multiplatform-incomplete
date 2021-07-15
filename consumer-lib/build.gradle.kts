import java.util.zip.ZipFile

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("maven-publish")
    id("com.h0tk3y.incomplete.consumer")
}

group = "com.h0tk3y.consumer"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("$rootDir/../build/repo")
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
    jvm { }
    js(IR) { }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("com.h0tk3y.incomplete:incomplete-lib:1.0")
            }
        }
        val jvmAndJsMain by creating {
            dependencies {
                api("com.h0tk3y.completing:completing-lib:1.0")
            }
        }
        val jvmMain by getting { dependsOn(jvmAndJsMain) }
        val jsMain by getting { dependsOn(jvmAndJsMain) }
    }
}