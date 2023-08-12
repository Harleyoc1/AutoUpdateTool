package com.harleyoconnor.autoupdatetool

import com.harleyoconnor.autoupdatetool.util.ModVersionInfo
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
            this.minecraftVersion.set("1.18.2")
            this.version.set("1.0.1")
            this.versionRecommended.set(false)
            this.updateCheckerFile.set(updateCheckerFile)
            this.changelogOutputFile.set(changelogOutputFile)
            this.debugMode.set(true)
        }

        val task = project.tasks.getByName("autoUpdate") as AutoUpdateTask

        assertEquals("1.18.2", task.minecraftVersion.get())
        assertEquals("1.0.1", task.version.get())
        assertEquals(false, task.versionRecommended.get())
        assertEquals(updateCheckerFile, task.updateCheckerFile.get().asFile)
        assertEquals(changelogOutputFile, task.changelogOutputFile.get().asFile)
        assertEquals(true, task.debugMode.get())
    }

    private val testVersionInfoJson = "{\n" +
            "  \"homepage\": \"https://example.com/\",\n" +
            "  \"1.20.1\": {\n" +
            "    \"1.20.1-1.0.0\": \"Initial release\"\n" +
            "  },\n" +
            "  \"promos\": {\n" +
            "    \"1.20.1-latest\": \"1.20.1-1.0.0\",\n" +
            "    \"1.20.1-recommended\": \"1.20.1-1.0.0\"\n" +
            "  }\n" +
            "}"

    @Test
    fun `test parsing version info`() {
        val versionInfo = ModVersionInfo.fromJson(testVersionInfoJson)
        val hashCode = versionInfo.hashCode()
        assertEquals(-1215134808, hashCode)
    }

    @Test
    fun `test serialising version info`() {
        val versionInfo = ModVersionInfo.fromJson(testVersionInfoJson)!!
        val versionInfoJson = ModVersionInfo.toJson(versionInfo)
        assertEquals(testVersionInfoJson, versionInfoJson)
    }

    @Test
    fun `test adding latest version`() {
        val versionInfo = ModVersionInfo.fromJson(testVersionInfoJson)!!
        versionInfo.addRecommendedVersion("1.20.1", "1.20.1-1.0.1-BETA1", "Added experimental stuff")
        assertEquals("Added experimental stuff", versionInfo.changelogs["1.20.1"]!!["1.20.1-1.0.1-BETA1"])
        assertEquals("1.20.1-1.0.1-BETA1", versionInfo.promos["1.20.1-latest"])
    }

    @Test
    fun `test adding latest version for new minecraft version`() {
        val versionInfo = ModVersionInfo.fromJson(testVersionInfoJson)!!
        versionInfo.addRecommendedVersion("1.19.4", "1.19.4-1.0.0-BETA1", "Backport to 1.19.4")
        assertEquals("Backport to 1.19.4", versionInfo.changelogs["1.19.4"]!!["1.19.4-1.0.0-BETA1"])
        assertEquals("1.19.4-1.0.0-BETA1", versionInfo.promos["1.19.4-latest"])
    }

    @Test
    fun `test adding recommended version`() {
        val versionInfo = ModVersionInfo.fromJson(testVersionInfoJson)!!
        versionInfo.addRecommendedVersion("1.20.1", "1.20.1-1.0.1", "Added stuff")
        assertEquals("Added stuff", versionInfo.changelogs["1.20.1"]!!["1.20.1-1.0.1"])
        assertEquals("1.20.1-1.0.1", versionInfo.promos["1.20.1-latest"])
        assertEquals("1.20.1-1.0.1", versionInfo.promos["1.20.1-recommended"])
    }

    @Test
    fun `test adding recommended version for new minecraft version`() {
        val versionInfo = ModVersionInfo.fromJson(testVersionInfoJson)!!
        versionInfo.addRecommendedVersion("1.19.4", "1.19.4-1.0.0", "Backport to 1.19.4")
        assertEquals("Backport to 1.19.4", versionInfo.changelogs["1.19.4"]!!["1.19.4-1.0.0"])
        assertEquals("1.19.4-1.0.0", versionInfo.promos["1.19.4-latest"])
        assertEquals("1.19.4-1.0.0", versionInfo.promos["1.19.4-recommended"])
    }

}
