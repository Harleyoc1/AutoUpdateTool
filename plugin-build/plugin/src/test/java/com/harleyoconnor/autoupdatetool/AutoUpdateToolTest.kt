package com.harleyoconnor.autoupdatetool

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

class AutoUpdateToolTest {

    @Test
    fun `plugin is applied correctly to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.harleyoconnor.autoupdatetool")

        assert(project.tasks.getByName("autoUpdate") is AutoUpdateTask)
    }

    @Test
    fun `extension autoUpdateTool is created correctly`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.harleyoconnor.autoupdatetool")

        assertNotNull(project.extensions.getByName("autoUpdateTool"))
    }

    @Test
    fun `parameters are passed correctly from extension to task`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.harleyoconnor.autoupdatetool")
        val updateCheckerFile = File(project.projectDir, ".update_checker.tmp")
        val changelogOutputFile = File(project.projectDir, ".changelog_output.tmp")
        (project.extensions.getByName("autoUpdateTool") as AutoUpdateToolExtension).apply {
            this.mcVersion.set("1.18.2")
            this.version.set("1.0.1")
            this.versionRecommended.set(false)
            this.updateCheckerFile.set(updateCheckerFile)
            this.changelogOutputFile.set(changelogOutputFile)
            this.debugMode.set(true)
        }

        val task = project.tasks.getByName("autoUpdate") as AutoUpdateTask

        assertEquals("1.18.2", task.mcVersion.get())
        assertEquals("1.0.1", task.version.get())
        assertEquals(false, task.versionRecommended.get())
        assertEquals(updateCheckerFile, task.updateCheckerFile.get().asFile)
        assertEquals(changelogOutputFile, task.changelogOutputFile.get().asFile)
        assertEquals(true, task.debugMode.get())
    }
}
