package com.corecmp.shared.auth

import android.content.Intent
import com.corecmp.shared.api.appContext
import com.corecmp.shared.api.isAppContextInitialized
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class GoogleAuth actual constructor() {
    actual val isAvailable: Boolean = true

    actual suspend fun signIn(webClientId: String?): SocialAuthResult? {
        if (!isAppContextInitialized()) {
            println("coreCmp error: CoreCmp.init(context) must be called in Application.onCreate() before using GoogleAuth.")
            return null
        }
        val context = appContext
        return suspendCancellableCoroutine { continuation ->
            GoogleSignInActivity.onResult = { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
            val intent = Intent(context, GoogleSignInActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (!webClientId.isNullOrBlank()) {
                    putExtra(GoogleSignInActivity.EXTRA_WEB_CLIENT_ID, webClientId)
                }
            }
            context.startActivity(intent)
        }
    }

    actual suspend fun signOut() {
        if (!isAppContextInitialized()) return
        try {
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
            val client = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(appContext, gso)
            client.signOut()
        } catch (_: Exception) {}
    }
}
