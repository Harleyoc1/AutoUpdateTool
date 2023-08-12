package com.harleyoconnor.autoupdatetool

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Optional
import javax.inject.Inject

const val DEFAULT_CHANGELOG_OUTPUT_FILE = "changelog.txt"

@Suppress("UnnecessaryAbstractClass")
abstract class AutoUpdateToolExtension @Inject constructor(project: Project) {

    private val objects = project.objects

    /**
     * The current Minecraft version.
     */
    val minecraftVersion: Property<String> = objects.property(String::class.java)

    /**
     * The new mod version. This is the version that the project will be updated to.
     */
    val version: Property<String> = objects.property(String::class.java)

    /**
     * Specifies whether the [version] being updated to is recommended. If `true`, this version will be set
     * as the recommended in the Forge update checker.
     *
     * Defaults to `true`.
     */
    val versionRecommended: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    /**
     * The file for the [Forge update checker](https://docs.minecraftforge.net/en/latest/misc/updatechecker/)
     * to be updated.
     *
     * If unset, the task will skip modifying the update checker file.
     */
    @Optional
    val updateCheckerFile: RegularFileProperty = objects.fileProperty()

    /**
     * The file to output the changelog at.
     *
     * This is used for the [CurseGradle](https://github.com/matthewprenger/CurseGradle) to read the changelog.
     * Note that for this to work the changelog property in the CurseGradle extension must be set to the same file.
     */
    val changelogOutputFile: RegularFileProperty = objects.fileProperty().convention(
        project.layout.buildDirectory.file(DEFAULT_CHANGELOG_OUTPUT_FILE)
    )

    /**
     * Specifies whether debug mode should be used. If `true`, debug output will be printed rather than actually
     * interacting with Git and writing file data.
     *
     * Defaults to `false`.
     */
    val debugMode: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
}
