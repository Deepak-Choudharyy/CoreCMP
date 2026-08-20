package com.corecmp.shared.platform

actual class QrScanner actual constructor() {
    actual fun startScan(onResult: QrScanCallback) {
        onResult.onError("QR scanning is not configured. Integrate CameraX / ML Kit in the host app.")
    }

    actual fun stopScan() = Unit

    actual val isAvailable: Boolean = false
}
