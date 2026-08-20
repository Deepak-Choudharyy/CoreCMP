package com.corecmp.shared.auth

expect class GoogleAuth() {
    val isAvailable: Boolean
    suspend fun signIn(webClientId: String? = null): SocialAuthResult?
    suspend fun signOut()
}

fun GoogleAuth.asProvider(webClientId: String? = null): SocialAuthProvider = object : SocialAuthProvider {
    override val type: SocialAuthProviderType = SocialAuthProviderType.GOOGLE
    override val isAvailable: Boolean get() = this@asProvider.isAvailable
    override suspend fun signIn(): SocialAuthResult? = this@asProvider.signIn(webClientId)
    override suspend fun signOut() = this@asProvider.signOut()
}
