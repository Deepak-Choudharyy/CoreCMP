package com.corecmp.shared.security

actual class BiometricAuth actual constructor() {
    actual val isAvailable: Boolean = false
    actual suspend fun authenticate(reason: String): Boolean = false
}
