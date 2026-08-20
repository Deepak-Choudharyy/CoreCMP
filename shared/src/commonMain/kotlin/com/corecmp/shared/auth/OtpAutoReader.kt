package com.corecmp.shared.auth

fun interface OtpAutoReadListener {
    fun onOtpReceived(otp: String)
    fun onError(message: String) = Unit
}

expect class OtpAutoReader() {
    fun startListening(listener: OtpAutoReadListener)
    fun stopListening()
    val isAvailable: Boolean
}
