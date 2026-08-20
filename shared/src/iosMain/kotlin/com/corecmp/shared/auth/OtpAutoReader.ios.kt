package com.corecmp.shared.auth

actual class OtpAutoReader actual constructor() {
    actual fun startListening(listener: OtpAutoReadListener) {
        listener.onError("OTP auto-read is not available on iOS.")
    }

    actual fun stopListening() = Unit

    actual val isAvailable: Boolean = false
}
