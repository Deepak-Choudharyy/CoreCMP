package com.corecmp.shared.notification

actual class PushTokenManager actual constructor() {
    actual suspend fun getToken(): String? = null

    actual fun onTokenRefresh(callback: (String) -> Unit) = Unit
}
