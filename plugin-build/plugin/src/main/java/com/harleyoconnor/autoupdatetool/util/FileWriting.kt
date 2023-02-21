package com.harleyoconnor.autoupdatetool.util

import java.io.File
import java.io.FileWriter

fun writeTextToFile(contents: String, file: File) {
    val writer = FileWriter(file)
    writer.write(contents)
    writer.close()
}
