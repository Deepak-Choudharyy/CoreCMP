package com.corecmp.shared.share

expect class ShareManager() {
    fun shareText(text: String)
    fun shareFile(bytes: ByteArray, fileName: String, mimeType: String)
}
