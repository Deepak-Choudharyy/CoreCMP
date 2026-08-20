package com.corecmp.shared.storage

import com.corecmp.shared.api.json
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString

private const val DRAFT_PREFIX = "corecmp_cmp_draft_"

class FormDraftManager internal constructor(
    private val storage: SecureStorage = SecureStorage(),
) {
    fun <T> save(formId: String, data: T, serializer: KSerializer<T>) {
        storage.putString(
            "$DRAFT_PREFIX$formId",
            json.encodeToString(serializer, data)
        )
        storage.putLong("${DRAFT_PREFIX}${formId}_ts", kotlin.time.Clock.System.now().toEpochMilliseconds())
    }

    fun <T> load(formId: String, serializer: KSerializer<T>): T? {
        val raw = storage.getString("$DRAFT_PREFIX$formId")
        if (raw.isBlank()) return null
        return runCatching { json.decodeFromString(serializer, raw) }.getOrNull()
    }

    fun savedAt(formId: String): Long =
        storage.getLong("${DRAFT_PREFIX}${formId}_ts", 0L)

    fun hasDraft(formId: String): Boolean =
        storage.getString("$DRAFT_PREFIX$formId").isNotBlank()

    fun clear(formId: String) {
        storage.remove("$DRAFT_PREFIX$formId")
        storage.remove("${DRAFT_PREFIX}${formId}_ts")
    }

    fun clearAllWithPrefix(prefix: String = "") {
        // SecureStorage has no list keys; host can call clear(formId) per form.
        clear(prefix)
    }
}
