package com.corecmp.shared.storage

import java.util.prefs.Preferences

actual class SecureStorage actual constructor() {
    private val prefs = Preferences.userNodeForPackage(SecureStorage::class.java)

    actual fun putString(key: String, value: String) {
        prefs.put(key, value)
    }

    actual fun getString(key: String, defaultValue: String): String {
        return prefs.get(key, defaultValue)
    }

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.putBoolean(key, value)
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    actual fun putInt(key: String, value: Int) {
        prefs.putInt(key, value)
    }

    actual fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }

    actual fun putLong(key: String, value: Long) {
        prefs.putLong(key, value)
    }

    actual fun getLong(key: String, defaultValue: Long): Long {
        return prefs.getLong(key, defaultValue)
    }

    actual fun remove(key: String) {
        prefs.remove(key)
    }

    actual fun clear() {
        prefs.clear()
    }
}
