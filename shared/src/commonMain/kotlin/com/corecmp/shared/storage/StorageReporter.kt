package com.corecmp.shared.storage

data class StorageUsage(
    val apiCacheBytes: Long = 0,
    val imageCacheBytes: Long = 0,
    val totalBytes: Long = apiCacheBytes + imageCacheBytes,
)

object StorageReporter {
    fun report(): StorageUsage {
        val apiDir = "${CacheFileIO.cacheDirectory()}/api_cache"
        val imageDir = "${CacheFileIO.cacheDirectory()}/image_cache"
        val apiBytes = directorySize(apiDir)
        val imageBytes = directorySize(imageDir)
        return StorageUsage(apiBytes, imageBytes)
    }

    fun clearAllCaches() {
        ApiCacheStorage().clearAll()
        ImageDiskCache.clear()
    }

    private fun directorySize(path: String): Long {
        return CacheFileIO.listFiles(path).sumOf { CacheFileIO.fileSize(it) }
    }
}
