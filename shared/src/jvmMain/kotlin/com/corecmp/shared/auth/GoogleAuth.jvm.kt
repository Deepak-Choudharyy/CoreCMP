package com.corecmp.shared.auth

actual class GoogleAuth actual constructor() {
    actual val isAvailable: Boolean = false

    actual suspend fun signIn(webClientId: String?): SocialAuthResult? = null

    actual suspend fun signOut() = Unit
}
