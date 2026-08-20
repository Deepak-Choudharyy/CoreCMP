package com.corecmp.shared.auth

/**
 * Stub for SMS Retriever / User Consent API integration.
 * Host apps should wire BroadcastReceiver and pass OTP via [deliverOtp].
 */
actual class OtpAutoReader actual constructor() {
    private var listener: OtpAutoReadListener? = null

    actual fun startListening(listener: OtpAutoReadListener) {
        this.listener = listener
    }

    actual fun stopListening() {
        listener = null
    }

    /** SMS Retriever requires host wiring — no SMS permission is used or needed. */
    actual val isAvailable: Boolean = false

    fun deliverOtp(otp: String) {
        listener?.onOtpReceived(otp)
    }
}
