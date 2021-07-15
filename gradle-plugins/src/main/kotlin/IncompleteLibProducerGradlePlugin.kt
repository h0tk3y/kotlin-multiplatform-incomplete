package com.h0tk3y.incomplete

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import javax.inject.Inject

open class IncompleteLibProducerPlugin @Inject constructor() : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("incomplete", IncompleteLibProducerExtension::class.java)
    }
}

open class IncompleteLibProducerExtension @Inject constructor() {
    @Suppress("unused") // Public API
    fun incompleteTarget(target: KotlinTarget, apiSetName: String = target.name) {
        configureIncompleteTarget(target, apiSetName)
    }
}

private fun configureIncompleteTarget(target: KotlinTarget, apiSetName: String) {
    val project = target.project

    target.compilations.all {
        compileKotlinTaskProvider.configure {
            enabled = false
        }
    }

    val incompleteLibMarkerDir = project.buildDir.resolve(
        "incompleteLibMarkers/${target.name}"
    )
    val generateIncompleteMarker = registerMarkerGeneratingTask(
        target, incompleteLibMarkerDir, MarkerKind.incomplete, project.group.toString(), project.name, apiSetName
    )
    target.compilations.getByName("main").output.classesDirs.from(
        project.files(incompleteLibMarkerDir).builtBy(generateIncompleteMarker)
    )
}