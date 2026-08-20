package com.corecmp.shared.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Lightweight file-based local store (SQLDelight alternative).
 * Supports schema version + migrations without adding a DB dependency.
 */
class LocalDataStore(
    private val namespace: String,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    private val metaPath = "${CacheFileIO.cacheDirectory()}/local_store/$namespace/meta.json"
    private val dataDir = "${CacheFileIO.cacheDirectory()}/local_store/$namespace/data"

    fun currentVersion(): Int = readMeta()?.version ?: 0

    fun migrate(
        targetVersion: Int,
        migrations: List<SchemaMigration>,
    ) {
        var version = currentVersion()
        if (version >= targetVersion) return
        migrations.sortedBy { it.fromVersion }.forEach { migration ->
            if (migration.fromVersion == version) {
                migration.migrate(this)
                version = migration.toVersion
                writeMeta(StoreMeta(version))
            }
        }
    }

    fun <T> put(key: String, value: T, serializer: KSerializer<T>) {
        CacheFileIO.write(dataPath(key), json.encodeToString(serializer, value))
    }

    fun <T> get(key: String, serializer: KSerializer<T>): T? {
        val raw = CacheFileIO.read(dataPath(key)) ?: return null
        return runCatching { json.decodeFromString(serializer, raw) }.getOrNull()
    }

    fun remove(key: String) = CacheFileIO.delete(dataPath(key))

    fun clear() = CacheFileIO.deleteAllIn(dataDir)

    private fun dataPath(key: String): String {
        val safe = key.hashCode().toUInt().toString(16)
        return "$dataDir/$safe.json"
    }

    private fun readMeta(): StoreMeta? {
        val raw = CacheFileIO.read(metaPath) ?: return null
        return runCatching { json.decodeFromString<StoreMeta>(raw) }.getOrNull()
    }

    private fun writeMeta(meta: StoreMeta) {
        CacheFileIO.write(metaPath, json.encodeToString(meta))
    }
}

@Serializable
private data class StoreMeta(val version: Int)

interface SchemaMigration {
    val fromVersion: Int
    val toVersion: Int
    fun migrate(store: LocalDataStore)
}
