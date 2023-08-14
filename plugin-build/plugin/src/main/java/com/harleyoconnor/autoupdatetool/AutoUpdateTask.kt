package com.harleyoconnor.autoupdatetool

import com.harleyoconnor.autoupdatetool.util.ModVersionInfo
import com.harleyoconnor.autoupdatetool.util.getCommitsSince
import com.harleyoconnor.autoupdatetool.util.getLastTag
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

abstract class AutoUpdateTask : DefaultTask() {

    init {
        description = "Automatic update task"
    }

    @get:Input
    @get:Option(option = "minecraftVersion", description = "The current Minecraft version.")
    abstract val minecraftVersion: Property<String>

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

    @get:Input
    @get:Option(
        option = "debugMode",
        description = "Specifies whether debug mode should be enabled."
    )
    abstract val debugMode: Property<Boolean>

    @TaskAction
    fun apply() {
        val changelog = buildChangelog().joinToString("\n")
        if (updateCheckerFile.isPresent) {
            updateUpdateCheckerFile(changelog)
        }
        if (debugMode.get()) {
            logger.lifecycle("Changelog output: {}", changelog)
        } else {
            writeTextToFile(changelog, changelogOutputFile.get().asFile)
        }
    }

    private fun buildChangelog(): List<String> {
        val lastVersion = getLastTag(project.projectDir)
        return getCommitsSince(lastVersion, project.projectDir)
            .filter { !it.message.trim().startsWith("[exclc]") }
            .map { "- ${it.message} [${it.authorName}]" }
            .collect(Collectors.toList())
    }

    private fun updateUpdateCheckerFile(changelog: String) {
        val updateCheckerFile = this.updateCheckerFile.get().asFile
        val versionInfo = ModVersionInfo.fromJson(updateCheckerFile.readText())!!
        updateVersionInfo(versionInfo, changelog)
        writeVersionInfo(versionInfo, updateCheckerFile)
    }

    private fun updateVersionInfo(versionInfo: ModVersionInfo, changelog: String) {
        val modVersion = "${minecraftVersion.get()}-${version.get()}"
        if (versionRecommended.get()) {
            versionInfo.addRecommendedVersion(minecraftVersion.get(), modVersion, changelog)
        } else {
            versionInfo.addLatestVersion(minecraftVersion.get(), modVersion, changelog)
        }
    }

    private fun writeVersionInfo(versionInfo: ModVersionInfo, updateCheckerFile: File) {
        if (debugMode.get()) {
            logger.lifecycle("Update checker Json output: {}", ModVersionInfo.toJson(versionInfo))
            return
        }
        try {
            ModVersionInfo.write(versionInfo, updateCheckerFile)
        } catch (e: Exception) {
            error("Error writing update checker file: " + e.message.toString())
        }
    }

}
