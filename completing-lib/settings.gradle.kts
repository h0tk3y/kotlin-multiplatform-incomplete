pluginManagement {
    includeBuild("../gradle-plugins")

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        kotlin("multiplatform").version("1.5.20")
        id("com.h0tk3y.incomplete.completing")
    }
}
