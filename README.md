# Auto Update Tool ![](https://img.shields.io/badge/Kotlin-1.8.10-7f52ff) [![GitHub](https://img.shields.io/github/license/Harleyoc1/AutoUpdateTool)](./LICENSE) ![](https://img.shields.io/github/actions/workflow/status/Harleyoc1/AutoUpdateTool/.github/workflows/pre-merge.yaml) [![](https://img.shields.io/github/v/tag/Harleyoc1/AutoUpdateTool)](https://github.com/Harleyoc1/AutoUpdateTool/releases)
A Gradle plugin for automatically publishing updates to Forge projects.

## What it does
The Auto Update Tool uses Git tags and commits to create a full changelog using commit messages since the last tag. This is intended to be combined with [CurseGradle](https://github.com/matthewprenger/CurseGradle) to automatically provide it with this changelog.

Currently, each commit is inserted into the changelog in this format:

`- <commit message> [<commit author>]`

Although this may be made modifiable at a later date.

## Guide
### Setup
_Note that all examples below are provided in a Kotlin build script. These will need to be converted to Groovy if you are not using a Kotlin build script._

1. Apply plugin to your Gradle project in the `plugins` block:

```kotlin
plugins {
    id("com.harleyoconnor.autoupdatetool")
}
```

2. Configure the extension:

```kotlin
autoUpdateTool {
    // Example of manually setting these, in practice you may like to use a Gradle property for these
    minecraftVersion.set("1.18.2")
    version.set("1.0.1")
    // If set to true, the recommended version in the update checker promos will be updated to the new version.
    versionRecommended.set(true)
    // Sets the Forge update checker file to update
    updateCheckerFile.set(file("version_info.json"))

    // Tells the CurseForge publish task to run after this (you will need to set this up using their own guide if you haven't already)
    finalizedBy("curseforge")
}
```

### Use
For first time use you must make sure to tag the commit for your previous version. After this, tags are created and pushed automatically by the tool.

1. Update project version. _It is recommended that you use a build script property so you only have to update this in one place._
2. Refresh Gradle project.
3. Run the `autoUpdate` task using Gradle. This may be done through your IDE or in the terminal using `./gradlew autoUpdate`.

At this point, if everything works a changelog will be outputted to the set output file and version info file if specified. If you have also setup the CurseGradle task to run after this, it should also have published the file on CurseForge with the same changelog.
