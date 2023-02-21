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
        val aFile = File(project.projectDir, ".tmp")
        (project.extensions.getByName("autoUpdateTool") as AutoUpdateToolExtension).apply {
            tag.set("a-sample-tag")
            message.set("just-a-message")
            outputFile.set(aFile)
        }

        val task = project.tasks.getByName("autoUpdate") as AutoUpdateTask

        assertEquals("a-sample-tag", task.tag.get())
        assertEquals("just-a-message", task.message.get())
        assertEquals(aFile, task.outputFile.get().asFile)
    }
}
