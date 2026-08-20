package com.corecmp.shared.storage

internal expect object CacheFileIO {
    fun cacheDirectory(): String
    fun read(path: String): String?
    fun readBytes(path: String): ByteArray?
    fun write(path: String, content: String)
    fun writeBytes(path: String, bytes: ByteArray)
    fun delete(path: String)
    fun deleteAllIn(directory: String)
    fun listFiles(directory: String): List<String>
    fun fileSize(path: String): Long
}
