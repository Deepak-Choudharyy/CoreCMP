package com.corecmp.shared.security

import com.corecmp.shared.storage.SecureStorage
import kotlin.time.Clock

data class ConsentRecord(
    val version: String,
    val acceptedAtEpochMs: Long,
    val purpose: String,
)

class ConsentManager(
    private val storage: SecureStorage = SecureStorage(),
    private val keyPrefix: String = "corecmp_cmp_consent_",
) {
    fun hasConsent(purpose: String, requiredVersion: String): Boolean {
        val savedVersion = storage.getString(key(purpose, "version"))
        val accepted = storage.getBoolean(key(purpose, "accepted"), false)
        return accepted && savedVersion == requiredVersion
    }

    fun recordConsent(purpose: String, version: String) {
        val now = Clock.System.now().toEpochMilliseconds()
        storage.putBoolean(key(purpose, "accepted"), true)
        storage.putString(key(purpose, "version"), version)
        storage.putLong(key(purpose, "accepted_at"), now)
    }

    fun revokeConsent(purpose: String) {
        storage.putBoolean(key(purpose, "accepted"), false)
        storage.remove(key(purpose, "version"))
        storage.remove(key(purpose, "accepted_at"))
    }

    fun getConsentRecord(purpose: String): ConsentRecord? {
        if (!storage.getBoolean(key(purpose, "accepted"), false)) return null
        val version = storage.getString(key(purpose, "version"))
        if (version.isBlank()) return null
        return ConsentRecord(
            version = version,
            acceptedAtEpochMs = storage.getLong(key(purpose, "accepted_at")),
            purpose = purpose,
        )
    }

    private fun key(purpose: String, field: String): String =
        "${keyPrefix}${purpose}_$field"
}
