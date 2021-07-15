pluginManagement {
    includeBuild("../gradle-plugins")

    repositories {
        google()
        mavenLocal()
        if (this == pluginManagement.repositories) {
            gradlePluginPortal()
        }
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.android.library")) {
                useModule("com.android.tools.build:gradle:${target.version}")
            }
        }
    }
    plugins {
        kotlin("multiplatform").version("1.5.20")
        id("com.android.library").version("4.2.0")
        id("com.h0tk3y.incomplete.consumer")
    }
}