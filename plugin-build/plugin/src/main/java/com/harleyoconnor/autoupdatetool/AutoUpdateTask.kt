package com.harleyoconnor.autoupdatetool

import com.google.gson.JsonObject
import com.harleyoconnor.autoupdatetool.util.addToGit
import com.harleyoconnor.autoupdatetool.util.asJsonObject
import com.harleyoconnor.autoupdatetool.util.commit
import com.harleyoconnor.autoupdatetool.util.fromJson
import com.harleyoconnor.autoupdatetool.util.getCommitsSince
import com.harleyoconnor.autoupdatetool.util.getLastTag
import com.harleyoconnor.autoupdatetool.util.getOrCreateJsonObject
import com.harleyoconnor.autoupdatetool.util.push
import com.harleyoconnor.autoupdatetool.util.pushTags
import com.harleyoconnor.autoupdatetool.util.tagNewVersion
import com.harleyoconnor.autoupdatetool.util.writeJsonToFile
import com.harleyoconnor.autoupdatetool.util.writeTextToFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File
import java.util.stream.Collectors
import kotlin.io.path.relativeTo

abstract class AutoUpdateTask : DefaultTask() {

    init {
        description = "Automatic update task"
    }

    @get:Input
    @get:Option(option = "mcVersion", description = "The current Minecraft version.")
    abstract val mcVersion: Property<String>

    @get:Input
    @get:Option(
        option = "modVersion",
        description = "The new mod version. This is the version that the project will be updated to."
    )
    abstract val version: Property<String>

    @get:Input
    @get:Option(
        option = "modVersionRecommended",
        description = "Specifies whether the version being updated to is recommended."
    )
    abstract val versionRecommended: Property<Boolean>

    @get:OutputFile
    @get:Optional
    abstract val updateCheckerFile: RegularFileProperty

    @get:OutputFile
    abstract val changelogOutputFile: RegularFileProperty

    @TaskAction
    fun apply() {
        val changelog = buildChangelog().joinToString("\n")
        if (updateCheckerFile.isPresent) {
            updateUpdateCheckerFile(changelog)
        }
        writeTextToFile(changelog, changelogOutputFile.get().asFile)
        tagNewVersion(version.get(), project.projectDir)
        pushTags(project.projectDir)
    }

    private fun buildChangelog(): List<String> {
        val lastVersion = getLastTag(project.projectDir)
        return getCommitsSince(lastVersion, project.projectDir)
            .map { "- ${it.message} [${it.authorName}]" }
            .collect(Collectors.toList())
    }

    private fun updateUpdateCheckerFile(changelog: String) {
        val updateCheckerFile = this.updateCheckerFile.get().asFile
        val json = fromJson(updateCheckerFile.readText()).asJsonObject(
            "Update checker Json invalid: root element must be an object."
        )
        updateUpdateCheckerJson(json, changelog)
        writeToUpdateCheckerFile(json, updateCheckerFile)
        commitAndPushChangesToUpdateCheckerFile()
    }

    private fun updateUpdateCheckerJson(json: JsonObject, changelog: String) {
        val changelogJson = json.getOrCreateJsonObject(
            mcVersion.get(),
            "Update check Json invalid: \"$mcVersion\" property must be an object."
        )
        // Update version changelog with pre-built changelog
        changelogJson.addProperty("$mcVersion-$version", changelog)

        val promosJson = json.getOrCreateJsonObject(
            "promos",
            "Update check Json invalid: \"promos\" property must be an object."
        )
        // Update promos Json with new version
        promosJson.addProperty("$mcVersion-latest", "$mcVersion-$version")
        if (versionRecommended.get()) {
            promosJson.addProperty("$mcVersion-recommended", "$mcVersion-$version")
        }
    }

    private fun writeToUpdateCheckerFile(json: JsonObject, updateCheckerFile: File) {
        try {
            writeJsonToFile(json, updateCheckerFile)
        } catch (e: Exception) {
            error("Error writing update checker file: " + e.message.toString())
        }
    }

    private fun commitAndPushChangesToUpdateCheckerFile() {
        val updateCheckerFile = this.updateCheckerFile.get().asFile
        val workingDir = updateCheckerFile.parentFile
        addToGit(updateCheckerFile.toPath().relativeTo(workingDir.toPath()).toString(), workingDir)
        commit("Update version info for ${project.displayName}", workingDir)
        push(workingDir)
    }
}
