package com.corecmp.shared.storage

object ImageDiskCache {
    private val dir = "${CacheFileIO.cacheDirectory()}/image_cache"

    fun pathForKey(key: String): String {
        val safe = key.hashCode().toUInt().toString(16)
        return "$dir/$safe.img"
    }

    fun put(key: String, bytes: ByteArray) {
        CacheFileIO.writeBytes(pathForKey(key), bytes)
    }

    fun get(key: String): ByteArray? {
        val path = pathForKey(key)
        return CacheFileIO.readBytes(path)
    }

    fun clear() {
        CacheFileIO.deleteAllIn(dir)
    }

    fun sizeBytes(): Long =
        CacheFileIO.listFiles(dir).sumOf { CacheFileIO.fileSize(it) }
}
