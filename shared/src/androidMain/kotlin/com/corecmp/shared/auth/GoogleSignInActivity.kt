package com.corecmp.shared.auth

import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoogleSignInActivity : ComponentActivity() {

    companion object {
        var onResult: ((SocialAuthResult?) -> Unit)? = null
        const val EXTRA_WEB_CLIENT_ID = "extra_web_client_id"
    }

    private var isFinished = false
    private var pendingSelectedEmail: String? = null
    private var webClientId: String? = null

    private val googleSignInLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intent = result.data
        var selectedEmail: String? = null
        var idTokenFromAccount: String? = null

        if (intent != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            try {
                val account = task.getResult(ApiException::class.java)
                selectedEmail = account?.email
                idTokenFromAccount = account?.idToken
            } catch (e: Exception) {
                println("coreCmp GoogleSignIn task exception: ${e.message}")
            }
        }

        if (selectedEmail.isNullOrBlank()) {
            try {
                val accounts = AccountManager.get(this).getAccountsByType("com.google")
                if (!accounts.isNullOrEmpty()) {
                    selectedEmail = accounts[0].name
                }
            } catch (_: Exception) {}
        }

        if (!selectedEmail.isNullOrBlank()) {
            fetchRealGoogleTokenAndFinish(selectedEmail, idTokenFromAccount)
        } else {
            finishWithResult(null)
        }
    }

    private val consentLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val email = pendingSelectedEmail
        if (result.resultCode == RESULT_OK && !email.isNullOrBlank()) {
            fetchRealGoogleTokenAndFinish(email, null)
        } else {
            finishWithResult(null)
        }
    }

    private fun fetchRealGoogleTokenAndFinish(selectedEmail: String, fallbackToken: String?) {
        pendingSelectedEmail = selectedEmail
        CoroutineScope(Dispatchers.IO).launch {
            var realToken: String? = fallbackToken
            var recoverableIntent: Intent? = null

            var fetchedIdToken: String? = if (fallbackToken?.startsWith("eyJ") == true) fallbackToken else null
            var fetchedAccessToken: String? = if (fallbackToken?.startsWith("ya29") == true) fallbackToken else null

            if (fetchedIdToken.isNullOrBlank() && !webClientId.isNullOrBlank()) {
                try {
                    val idTokenScope = "oauth2:server:client_id:$webClientId"
                    val tokenResult = GoogleAuthUtil.getToken(this@GoogleSignInActivity, selectedEmail, idTokenScope)
                    if (tokenResult?.startsWith("eyJ") == true) {
                        fetchedIdToken = tokenResult
                    } else if (tokenResult?.startsWith("ya29") == true) {
                        fetchedAccessToken = tokenResult
                    }
                } catch (e: UserRecoverableAuthException) {
                    recoverableIntent = e.intent
                } catch (e: Exception) {
                    if (e.javaClass.simpleName.contains("UserRecoverable")) {
                        try {
                            val getIntentMethod = e.javaClass.getMethod("getIntent")
                            recoverableIntent = getIntentMethod.invoke(e) as? Intent
                        } catch (_: Exception) {}
                    }
                }
            }

            if (fetchedAccessToken.isNullOrBlank() && recoverableIntent == null) {
                try {
                    val accessTokenScope = "oauth2:https://www.googleapis.com/auth/userinfo.email openid profile"
                    val tokenResult = GoogleAuthUtil.getToken(this@GoogleSignInActivity, selectedEmail, accessTokenScope)
                    if (tokenResult?.startsWith("ya29") == true || tokenResult?.startsWith("oauth2") == true) {
                        fetchedAccessToken = tokenResult
                    } else if (fetchedIdToken == null && tokenResult?.startsWith("eyJ") == true) {
                        fetchedIdToken = tokenResult
                    }
                } catch (e: UserRecoverableAuthException) {
                    if (recoverableIntent == null) recoverableIntent = e.intent
                } catch (_: Exception) {}
            }

            if (recoverableIntent != null) {
                runOnUiThread {
                    try {
                        consentLauncher.launch(recoverableIntent)
                    } catch (_: Exception) {
                        finishWithResult(null)
                    }
                }
                return@launch
            }

            val authResult = SocialAuthResult(
                provider = SocialAuthProviderType.GOOGLE,
                idToken = fetchedIdToken,
                accessToken = fetchedAccessToken,
                email = selectedEmail,
                displayName = selectedEmail.substringBefore("@")
            )
            finishWithResult(authResult)
        }
    }

    private fun finishWithResult(result: SocialAuthResult?) {
        if (isFinished) return
        isFinished = true
        onResult?.invoke(result)
        onResult = null
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finishWithResult(null)
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webClientId = intent.getStringExtra(EXTRA_WEB_CLIENT_ID)
        if (webClientId.isNullOrBlank()) {
            try {
                val resId = resources.getIdentifier("default_web_client_id", "string", packageName)
                if (resId != 0) {
                    webClientId = getString(resId)
                }
            } catch (_: Exception) {}
        }

        val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()

        if (!webClientId.isNullOrBlank()) {
            gsoBuilder.requestIdToken(webClientId!!)
        }

        val client = GoogleSignIn.getClient(this, gsoBuilder.build())

        client.signOut().addOnCompleteListener {
            try {
                googleSignInLauncher.launch(client.signInIntent)
            } catch (e: Exception) {
                println("coreCmp GoogleSignIn launch error: ${e.message}")
                finishWithResult(null)
            }
        }
    }
}
