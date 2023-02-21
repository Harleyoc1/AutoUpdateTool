plugins {
    java
    id("com.harleyoconnor.autoupdatetool")
}

autoUpdateTool {
    // Example of manually setting these, in practice you may like to use a Gradle property for these
    mcVersion.set("1.18.2")
    version.set("1.0.1")
    // If set to true, the recommended version in the update checker promos will be updated to the new version.
    versionRecommended.set(true)
    // Sets the Forge update checker file to update
    updateCheckerFile.set(file("version_info.json"))
}
