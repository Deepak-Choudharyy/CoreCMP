package com.corecmp.shared.auth

data class SocialAuthResult(
    val provider: SocialAuthProviderType,
    val idToken: String? = null,
    val accessToken: String? = null,
    val email: String? = null,
    val displayName: String? = null,
)

enum class SocialAuthProviderType {
    GOOGLE,
    APPLE,
}

interface SocialAuthProvider {
    val type: SocialAuthProviderType
    val isAvailable: Boolean
    suspend fun signIn(): SocialAuthResult?
    suspend fun signOut()
}
