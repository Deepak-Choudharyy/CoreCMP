package com.corecmp.shared.testing

/**
 * In-memory storage with the same API as [SecureStorage] for unit tests and previews.
 */
class FakeSecureStorage {
    private val strings = mutableMapOf<String, String>()
    private val booleans = mutableMapOf<String, Boolean>()
    private val ints = mutableMapOf<String, Int>()
    private val longs = mutableMapOf<String, Long>()

    fun putString(key: String, value: String) { strings[key] = value }
    fun getString(key: String, defaultValue: String = ""): String = strings[key] ?: defaultValue

    fun putBoolean(key: String, value: Boolean) { booleans[key] = value }
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean = booleans[key] ?: defaultValue

    fun putInt(key: String, value: Int) { ints[key] = value }
    fun getInt(key: String, defaultValue: Int = 0): Int = ints[key] ?: defaultValue

    fun putLong(key: String, value: Long) { longs[key] = value }
    fun getLong(key: String, defaultValue: Long = 0L): Long = longs[key] ?: defaultValue

    fun remove(key: String) {
        strings.remove(key)
        booleans.remove(key)
        ints.remove(key)
        longs.remove(key)
    }

    fun clear() {
        strings.clear()
        booleans.clear()
        ints.clear()
        longs.clear()
    }
}
