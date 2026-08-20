package com.corecmp.shared.notification

expect class PushTokenManager() {
    suspend fun getToken(): String?
    fun onTokenRefresh(callback: (String) -> Unit)
}
