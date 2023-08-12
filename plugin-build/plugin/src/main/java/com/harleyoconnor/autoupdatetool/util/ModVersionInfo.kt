package com.harleyoconnor.autoupdatetool.util

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import java.io.File

class ModVersionInfo(
    val homepage: String,
    val changelogs: MutableMap<String, MutableMap<String, String>>,
    val promos: MutableMap<String, String>
) {

    companion object {

        private val ADAPTER = MOSHI.adapter(ModVersionInfo::class.java)

        fun fromJson(json: String): ModVersionInfo? {
            return ADAPTER.fromJson(json)
        }

        fun toJson(modVersionInfo: ModVersionInfo): String {
            return ADAPTER.toJson(modVersionInfo)
        }

        fun write(modVersionInfo: ModVersionInfo, file: File) {
            writeTextToFile(toJson(modVersionInfo), file)
        }
    }

    class Adapter: JsonAdapter<ModVersionInfo>() {

        private val stringToStringMapAdapter: Lazy<JsonAdapter<Map<String, String>>> = lazy {
            MOSHI.adapter(Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))
        }

        @FromJson
        override fun fromJson(reader: JsonReader): ModVersionInfo? {
            if (reader.peek() == JsonReader.Token.NULL) {
                return null
            }
            reader.beginObject()
            var homepage = ""
            val changelogs: MutableMap<String, MutableMap<String, String>> = HashMap()
            var promos: Map<String, String> = emptyMap()
            while (reader.peek() == JsonReader.Token.NAME) {
                val name = reader.nextName()
                when (name) {
                    "homepage" -> homepage = reader.nextString()
                    "promos" -> promos = stringToStringMapAdapter.value.fromJson(reader) ?: emptyMap()
                    else -> {
                        changelogs[name] = stringToStringMapAdapter.value.fromJson(reader)?.toMutableMap() ?: mutableMapOf()
                    }
                }
            }
            reader.endObject()
            return ModVersionInfo(homepage, changelogs, promos.toMutableMap())
        }

        @ToJson
        override fun toJson(writer: JsonWriter, value: ModVersionInfo?) {
            if (value == null) {
                writer.nullValue()
                return
            }
            writer.beginObject()
            writer.name("homepage").value(value.homepage)
            for (entry in value.changelogs) {
                writer.name(entry.key).beginObject()
                for (changelogEntry in entry.value) {
                    writer.name(changelogEntry.key).value(changelogEntry.value)
                }
                writer.endObject()
            }
            writer.name("promos")
            stringToStringMapAdapter.value.toJson(writer, value.promos)
            writer.endObject()
        }
    }

    fun addRecommendedVersion(minecraftVersion: String, modVersion: String, changelog: String) {
        addLatestVersion(minecraftVersion, modVersion, changelog)
        promos["$minecraftVersion-recommended"] = modVersion
    }

    fun addLatestVersion(minecraftVersion: String, modVersion: String, changelog: String) {
        addChangelog(minecraftVersion, modVersion, changelog)
        promos["$minecraftVersion-latest"] = modVersion
    }

    private fun addChangelog(minecraftVersion: String, modVersion: String, changelog: String) {
        changelogs.computeIfAbsent(minecraftVersion) { HashMap() } [modVersion] = changelog
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModVersionInfo

        if (homepage != other.homepage) return false
        if (changelogs != other.changelogs) return false
        return promos == other.promos
    }

    override fun hashCode(): Int {
        var result = homepage.hashCode()
        result = 31 * result + changelogs.hashCode()
        result = 31 * result + promos.hashCode()
        return result
    }

}
