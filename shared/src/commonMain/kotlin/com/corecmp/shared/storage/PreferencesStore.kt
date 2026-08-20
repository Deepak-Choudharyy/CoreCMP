package com.corecmp.shared.storage

import com.corecmp.shared.api.provideSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class PreferencesStore(
    private val settings: Settings = provideSettings(),
) {
    fun putString(key: String, value: String) = settings.putString(key, value)
    fun getString(key: String, default: String = ""): String = settings.getString(key, default)

    fun putBoolean(key: String, value: Boolean) = settings.putBoolean(key, value)
    fun getBoolean(key: String, default: Boolean = false): Boolean = settings.getBoolean(key, default)

    fun putInt(key: String, value: Int) = settings.putInt(key, value)
    fun getInt(key: String, default: Int = 0): Int = settings.getInt(key, default)

    fun putLong(key: String, value: Long) = settings.putLong(key, value)
    fun getLong(key: String, default: Long = 0L): Long = settings.getLong(key, default)

    fun remove(key: String) = settings.remove(key)
    fun clear() = settings.clear()
}
