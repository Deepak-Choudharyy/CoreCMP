package com.corecmp.shared.storage

import java.io.File

internal actual object CacheFileIO {
    actual fun cacheDirectory(): String {
        val tmp = System.getProperty("java.io.tmpdir") ?: "/tmp"
        return "$tmp/corecmp_cache"
    }

    actual fun read(path: String): String? {
        val file = File(path)
        return if (file.exists()) file.readText() else null
    }

    actual fun readBytes(path: String): ByteArray? {
        val file = File(path)
        return if (file.exists()) file.readBytes() else null
    }

    actual fun write(path: String, content: String) {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    actual fun writeBytes(path: String, bytes: ByteArray) {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
    }

    actual fun delete(path: String) {
        File(path).delete()
    }

    actual fun deleteAllIn(directory: String) {
        File(directory).listFiles()?.forEach { it.delete() }
    }

    actual fun listFiles(directory: String): List<String> =
        File(directory).listFiles()?.map { it.absolutePath } ?: emptyList()

    actual fun fileSize(path: String): Long = File(path).length()
}
