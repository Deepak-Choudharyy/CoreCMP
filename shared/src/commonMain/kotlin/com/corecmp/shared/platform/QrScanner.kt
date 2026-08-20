package com.corecmp.shared.platform

fun interface QrScanCallback {
    fun onResult(text: String)
    fun onError(message: String) = Unit
    fun onCancelled() = Unit
}

expect class QrScanner() {
    fun startScan(onResult: QrScanCallback)
    fun stopScan()
    val isAvailable: Boolean
}
