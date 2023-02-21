package com.harleyoconnor.autoupdatetool.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.File
import java.io.StringWriter

private val GSON = GsonBuilder().setPrettyPrinting().create()

fun fromJson(json: String): JsonElement {
    return GSON.fromJson(json, JsonElement::class.java)
}

fun writeJsonToFile(json: JsonElement, file: File) {
    writeTextToFile(getJsonAsString(json), file)
}

fun getJsonAsString(json: JsonElement): String {
    val writer = StringWriter()
    GSON.toJson(json, writer)
    return writer.toString()
}

/**
 * @param errorMessage message to display if this json element is not a json object
 */
fun JsonElement.asJsonObject(errorMessage: String): JsonObject {
    if (this.isJsonObject) {
        return this.asJsonObject
    } else {
        error(errorMessage)
    }
}

/**
 * @param errorMessage message to display if the element with the specified key exists and is not a json object
 */
fun JsonObject.getOrCreateJsonObject(key: String, errorMessage: String): JsonObject {
    val json: JsonObject
    if (this.has(key)) {
        if (!this.get(key).isJsonObject) {
            error(errorMessage)
        }
        json = this.getAsJsonObject(key)
    } else {
        json = JsonObject()
        this.add(key, json)
    }
    return json
}
