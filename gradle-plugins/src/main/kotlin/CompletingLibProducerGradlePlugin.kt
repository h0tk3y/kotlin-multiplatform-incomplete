package com.h0tk3y.incomplete

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceTask
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget
import java.lang.UnsupportedOperationException
import javax.inject.Inject

open class CompletingLibProducerPlugin @Inject constructor() : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("completing", CompletingLibProducerExtension::class.java)
    }
}

open class CompletingLibProducerExtension @Inject constructor(private val project: Project) {
    @Suppress("unused") // Public API
    fun completingTarget(
        target: KotlinTarget,
        incompleteLibDependency: String,
        incompleteLibSources: FileCollection,
        apiSetName: String = target.name
    ) {
        configureCompletingTarget(target, incompleteLibDependency, incompleteLibSources, apiSetName)
    }

    fun completingMultiplatformLibrary(
        incompleteLibDependency: String,
        incompleteLibSources: FileCollection,
        apiSetName: (KotlinTarget) -> String
    ) {
        if (!project.pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            throw GradleException("Completing a library with a multiplatform library requires Kotlin Multiplatform")
        }

        val ext = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        ext.targets.matching { it !is KotlinMetadataTarget }.all {
            completingTarget(this@all, incompleteLibDependency, incompleteLibSources, apiSetName(this@all))
        }

        // Add a new source set that will bring the incomplete lib's sources to the platform compilations and, what's
        // also important, won't compile the incomplete lib's sources to metadata, but instead will depend on the lib
        val incompleteMain = ext.sourceSets.create("incompleteMain")
        ext.sourceSets.getByName("commonMain").dependsOn(incompleteMain)
        incompleteMain.dependencies { api(incompleteLibDependency) }
        if (project.providers.systemProperty("idea.active").isPresent) {
            incompleteMain.kotlin.setSrcDirs(incompleteLibSources)
        }
        ext.targets.getByName("metadata").compilations.matching { it.name == incompleteMain.name }.all {
            compileKotlinTaskProvider.configure {
                enabled = false
            }
        }
    }
}

private fun configureCompletingTarget(
    target: KotlinTarget,
    incompleteLibDependency: String,
    incompleteLibSources: FileCollection,
    apiSetName: String
) {
    val project = target.project
    val dependency = project.dependencies.create(incompleteLibDependency) as ModuleDependency

    if (target is KotlinAndroidTarget) {
        throw UnsupportedOperationException("Android targets are not yet supported")
    }

    val completingLibMarkerDir = project.buildDir.resolve(
        "completingLibMarkers/${target.name}"
    )
    val generateIncompleteMarker = registerMarkerGeneratingTask(
        target, completingLibMarkerDir, MarkerKind.completing, dependency.group.orEmpty(), dependency.name, apiSetName
    )

    target.compilations.getByName("main") {
        defaultSourceSet.dependencies { api(dependency) }

        compileKotlinTaskProvider.configure {
            (this as SourceTask).source(incompleteLibSources)
            kotlinOptions.freeCompilerArgs += listOf(
                "-Xmulti-platform=true",
                "-Xcommon-sources=${incompleteLibSources.asPath}"
            )
        }

        output.classesDirs.from(
            project.files(completingLibMarkerDir).builtBy(generateIncompleteMarker)
        )
    }

}