package com.corecmp.shared.auth

expect class AppleAuth() {
    val isAvailable: Boolean
    suspend fun signIn(): SocialAuthResult?
    suspend fun signOut()
}

fun AppleAuth.asProvider(): SocialAuthProvider = object : SocialAuthProvider {
    override val type: SocialAuthProviderType = SocialAuthProviderType.APPLE
    override val isAvailable: Boolean get() = this@asProvider.isAvailable
    override suspend fun signIn(): SocialAuthResult? = this@asProvider.signIn()
    override suspend fun signOut() = this@asProvider.signOut()
}
