package com.h0tk3y.incomplete

import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import java.io.File
import java.util.zip.ZipFile

internal enum class MarkerKind {
    incomplete, completing
}

internal fun registerMarkerGeneratingTask(
    target: KotlinTarget,
    markerBuildDir: File,
    markerKind: MarkerKind,
    groupId: String,
    moduleId: String,
    apiSetName: String
) = target.project.tasks.register("generate${target.name.capitalize()}Marker") {
    outputs.dir(markerBuildDir)
    inputs.property("markerKind", markerKind)
    inputs.property("groupId", groupId)
    inputs.property("moduleId", moduleId)
    inputs.property("apiSetName", apiSetName)
    doLast {
        markerBuildDir.deleteRecursively()
        markerBuildDir.resolve("META-INF").run {
            mkdirs()
            // TODO: use a better format than \n-separated strings
            val incompleteMarker = markerContent(groupId, moduleId, apiSetName)
            val entryName = markerEntryName(groupId, moduleId, markerKind)
            resolve(entryName).writeText(incompleteMarker)
        }
    }
}

private fun markerEntryName(groupId: String, moduleId: String, markerKind: MarkerKind) =
    "$groupId.$moduleId.$markerKind"

private fun markerContent(groupId: String, moduleId: String, apiSetName: String) =
    listOf(groupId, moduleId, apiSetName).joinToString("\n")

internal data class IncompleteVariantId(val group: String, val name: String, val apiSetName: String)

internal fun extractMarkers(fromFiles: Iterable<File>, markerKind: MarkerKind): Iterable<IncompleteVariantId> {
    val idSuffix = "." + markerKind.toString()
    return fromFiles.filter { it.extension in setOf("jar", "klib") }.flatMap {
        ZipFile(it).use { zip ->
            val matching = zip.entries().asSequence().filter { it.name.endsWith(idSuffix) }
            matching.mapTo(mutableListOf()) { entry ->
                val text = zip.getInputStream(entry).use { it.reader().readText() }
                val (group, name, variant) = text.lines()
                IncompleteVariantId(group, name, variant)
            }
        }
    }
}