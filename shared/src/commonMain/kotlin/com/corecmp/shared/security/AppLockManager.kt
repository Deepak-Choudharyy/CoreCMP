package com.corecmp.shared.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.corecmp.shared.storage.SecureStorage

private const val KEY_LOCK_ENABLED = "corecmp_cmp_lock_enabled"
private const val KEY_PIN_HASH = "corecmp_cmp_pin_hash"

class AppLockManager internal constructor(
    private val storage: SecureStorage = SecureStorage(),
    private val biometric: BiometricAuth = BiometricAuth(),
) {
    var isUnlocked by mutableStateOf(!isEnabled())
        private set

    val isBiometricAvailable: Boolean
        get() = biometric.isAvailable

    fun isEnabled(): Boolean = storage.getBoolean(KEY_LOCK_ENABLED, false)

    fun enable(pin: String) {
        require(pin.length >= 4) { "PIN must be at least 4 digits" }
        storage.putString(KEY_PIN_HASH, hashPin(pin))
        storage.putBoolean(KEY_LOCK_ENABLED, true)
        isUnlocked = true
    }

    fun disable(pin: String): Boolean {
        if (!verifyPin(pin)) return false
        storage.putBoolean(KEY_LOCK_ENABLED, false)
        storage.remove(KEY_PIN_HASH)
        isUnlocked = true
        return true
    }

    fun verifyPin(pin: String): Boolean {
        val saved = storage.getString(KEY_PIN_HASH)
        if (saved.isBlank()) return false
        return saved == hashPin(pin)
    }

    fun unlockWithPin(pin: String): Boolean {
        val ok = verifyPin(pin)
        if (ok) isUnlocked = true
        return ok
    }

    suspend fun unlockWithBiometric(reason: String = "Unlock app"): Boolean {
        if (!isEnabled()) {
            isUnlocked = true
            return true
        }
        val ok = biometric.authenticate(reason)
        if (ok) isUnlocked = true
        return ok
    }

    fun lock() {
        if (isEnabled()) isUnlocked = false
    }
}

expect class BiometricAuth() {
    val isAvailable: Boolean
    suspend fun authenticate(reason: String): Boolean
}

internal fun hashPin(pin: String): String {
    var hash = 0L
    pin.forEach { hash = 31 * hash + it.code }
    return hash.toString(16)
}
