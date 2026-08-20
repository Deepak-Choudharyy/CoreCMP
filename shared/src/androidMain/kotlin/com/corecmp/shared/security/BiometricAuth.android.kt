package com.corecmp.shared.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.corecmp.shared.api.appContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class BiometricAuth actual constructor() {

    actual val isAvailable: Boolean
        get() {
            val result = BiometricManager.from(appContext)
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            return result == BiometricManager.BIOMETRIC_SUCCESS
        }

    actual suspend fun authenticate(reason: String): Boolean {
        val activity = appContext as? FragmentActivity ?: return false
        val executor = ContextCompat.getMainExecutor(appContext)

        return suspendCancellableCoroutine { cont ->
            val prompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (cont.isActive) cont.resume(true)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (cont.isActive) cont.resume(false)
                    }

                    override fun onAuthenticationFailed() = Unit
                }
            )

            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock")
                .setSubtitle(reason)
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()

            prompt.authenticate(info)
        }
    }
}
