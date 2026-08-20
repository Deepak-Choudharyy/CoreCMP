@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlinx.cinterop.BetaInteropApi::class
)

package com.corecmp.shared.storage

import com.corecmp.shared.getCacheDir
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

internal actual object CacheFileIO {
    actual fun cacheDirectory(): String =
        getCacheDir().removeSuffix("/image_cache")

    actual fun read(path: String): String? {
        if (!NSFileManager.defaultManager.fileExistsAtPath(path)) return null
        return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) as? String
    }

    actual fun readBytes(path: String): ByteArray? =
        NSData.dataWithContentsOfFile(path)?.toByteArray()

    actual fun write(path: String, content: String) {
        val fm = NSFileManager.defaultManager
        val parent = path.substringBeforeLast('/', missingDelimiterValue = "")
        if (parent.isNotEmpty()) {
            fm.createDirectoryAtPath(parent, true, null, null)
        }
        NSString.create(string = content).writeToFile(path, true, NSUTF8StringEncoding, null)
    }

    actual fun writeBytes(path: String, bytes: ByteArray) {
        val fm = NSFileManager.defaultManager
        val parent = path.substringBeforeLast('/', missingDelimiterValue = "")
        if (parent.isNotEmpty()) {
            fm.createDirectoryAtPath(parent, true, null, null)
        }
        bytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = bytes.size.convert(),
            ).writeToFile(path, true)
        }
    }

    actual fun delete(path: String) {
        NSFileManager.defaultManager.removeItemAtPath(path, null)
    }

    actual fun deleteAllIn(directory: String) {
        val fm = NSFileManager.defaultManager
        val contents = fm.contentsOfDirectoryAtPath(directory, null) as? List<*>
        contents?.forEach { name ->
            fm.removeItemAtPath("$directory/$name", null)
        }
    }

    actual fun listFiles(directory: String): List<String> {
        val fm = NSFileManager.defaultManager
        val contents = fm.contentsOfDirectoryAtPath(directory, null) as? List<*>
        return contents?.mapNotNull { name -> "$directory/$name" } ?: emptyList()
    }

    actual fun fileSize(path: String): Long {
        val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(path, null)
        val size = attrs?.get("NSFileSize") as? platform.Foundation.NSNumber
        return size?.longValue ?: 0L
    }
}

private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return byteArrayOf()
    return ByteArray(size).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }
}
