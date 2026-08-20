package com.corecmp.shared.storage

import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings

actual class SecureStorage actual constructor() {
    private val delegate: Settings = KeychainSettings()

    actual fun putString(key: String, value: String) {
        delegate.putString(key, value)
    }

    actual fun getString(key: String, defaultValue: String): String {
        return delegate.getString(key, defaultValue)
    }

    actual fun putBoolean(key: String, value: Boolean) {
        delegate.putBoolean(key, value)
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return delegate.getBoolean(key, defaultValue)
    }

    actual fun putInt(key: String, value: Int) {
        delegate.putInt(key, value)
    }

    actual fun getInt(key: String, defaultValue: Int): Int {
        return delegate.getInt(key, defaultValue)
    }

    actual fun putLong(key: String, value: Long) {
        delegate.putLong(key, value)
    }

    actual fun getLong(key: String, defaultValue: Long): Long {
        return delegate.getLong(key, defaultValue)
    }

    actual fun remove(key: String) {
        delegate.remove(key)
    }

    actual fun clear() {
        delegate.clear()
    }
}
