package com.h0tk3y.incomplete

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import javax.inject.Inject

open class IncompleteLibConsumerPlugin @Inject constructor() : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            target.extensions.getByType<KotlinMultiplatformExtension>().targets.all(::configureTarget)
        }
        target.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            configureTarget(target.extensions.getByType<KotlinSingleTargetExtension>().target)
        }
        target.pluginManager.withPlugin("org.jetbrains.kotlin.js") {
            configureTarget(target.extensions.getByType<KotlinSingleTargetExtension>().target)
        }
        target.pluginManager.withPlugin("org.jetbrains.kotlin.android") {
            configureTarget(target.extensions.getByType<KotlinSingleTargetExtension>().target)
        }
    }

    private fun configureTarget(target: KotlinTarget) {
        val productionCompilations = target.compilations.matching {
            when (target) {
                is KotlinAndroidTarget -> "Test" !in it.name
                else -> it.name == "main"
            }
        }
        productionCompilations.all {
            compileKotlinTaskProvider.configure {
                doFirst {
                    val incompleteIds = extractMarkers(compileDependencyFiles, MarkerKind.incomplete)
                    val completingIds = extractMarkers(compileDependencyFiles, MarkerKind.completing)
                    val unsatisfied = incompleteIds - completingIds
                    if (unsatisfied.isNotEmpty()) {
                        throw GradleException(
                            "Target ${target.name} has unsatisfied incomplete libraries on the classpath:" +
                                unsatisfied.joinToString("\n", "\n", "\n") {
                                    "  * ${it.group}:${it.name} (the ${it.apiSetName} API)"
                                } +
                                "Please add the completing libraries to the target's dependencies."
                        )
                    }
                }
            }
        }
    }

}