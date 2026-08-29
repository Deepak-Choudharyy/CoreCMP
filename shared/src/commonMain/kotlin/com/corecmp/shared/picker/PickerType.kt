package com.corecmp.shared.picker

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

enum class PickerType {

    CAMERA,
    IMAGE,
    DOCUMENT

}

data class DocumentConfig(
    val mimeTypes: List<String>
)


data class PickedFile(
    val bytes: ByteArray,
    val fileName: String?,
    val mimeType: String?
) {

    val isImage: Boolean
        get() = mimeType?.startsWith("image/") == true

    val isPdf: Boolean
        get() = mimeType == "application/pdf"

    val sizeInMb: Double
        get() = bytes.size / (1024.0 * 1024.0)

    fun isUnder10Mb(): Boolean {
        return bytes.size <= 10 * 1024 * 1024
    }
    fun isUnder4Mb(): Boolean {
        return bytes.size <= 4 * 1024 * 1024
    }
    fun isUnder2Mb(): Boolean {
        return bytes.size <= 2 * 1024 * 1024
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedFile) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (fileName != other.fileName) return false
        if (mimeType != other.mimeType) return false
        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + (fileName?.hashCode() ?: 0)
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        return result
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun PickedFile.toBase64Image(): String {
    return Base64.encode(this.bytes)
}