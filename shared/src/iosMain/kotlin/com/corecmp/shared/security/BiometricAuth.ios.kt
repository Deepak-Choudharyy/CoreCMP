package com.corecmp.shared.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual class BiometricAuth actual constructor() {

    actual val isAvailable: Boolean
        get() {
            val context = LAContext()
            memScoped {
                val error = alloc<ObjCObjectVar<platform.Foundation.NSError?>>()
                return context.canEvaluatePolicy(
                    LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                    error.ptr
                )
            }
        }

    actual suspend fun authenticate(reason: String): Boolean {
        val context = LAContext()
        return suspendCancellableCoroutine { cont ->
            context.evaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                localizedReason = reason
            ) { success, _ ->
                if (cont.isActive) cont.resume(success)
            }
        }
    }
}
