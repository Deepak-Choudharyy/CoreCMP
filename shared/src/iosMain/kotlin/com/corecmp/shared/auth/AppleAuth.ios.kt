package com.corecmp.shared.auth

actual class AppleAuth actual constructor() {
    actual val isAvailable: Boolean = false

    actual suspend fun signIn(): SocialAuthResult? = null

    actual suspend fun signOut() = Unit
}
