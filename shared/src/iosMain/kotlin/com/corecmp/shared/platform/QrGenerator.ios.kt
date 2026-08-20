package com.corecmp.shared.platform

actual fun generateQrPngBytes(text: String, sizePx: Int): ByteArray? = null

actual class QrGenerator actual constructor() {
    actual fun generatePng(text: String, sizePx: Int): ByteArray? = null
}
