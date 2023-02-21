package com.harleyoconnor.autoupdatetool

import org.gradle.api.Plugin
import org.gradle.api.Project

const val EXTENSION_NAME = "autoUpdateTool"
const val TASK_NAME = "autoUpdate"

abstract class AutoUpdateTool : Plugin<Project> {
    override fun apply(project: Project) {
        // Add the 'template' extension object
        val extension = project.extensions.create(EXTENSION_NAME, AutoUpdateToolExtension::class.java, project)

        // Add a task that uses configuration from the extension object
        project.tasks.register(TASK_NAME, AutoUpdateTask::class.java) {
            it.mcVersion.set(extension.mcVersion)
            it.version.set(extension.version)
            it.versionRecommended.set(extension.versionRecommended)
            it.updateCheckerFile.set(extension.updateCheckerFile)
            it.changelogOutputFile.set(extension.changelogOutputFile)
        }
    }
}
