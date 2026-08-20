package com.corecmp.shared.api

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

class SharedViewModel(
    val settings: Settings,
    val json: Json
) : ViewModel() {
    fun getString(key: String): String {
        return settings.getString(key, "")
    }

    fun setString(key: String, value: String?) {
        settings.putString(key, value ?: "")
    }

    inline fun <reified T> setObject(
        key: String,
        value: T?
    ) {
        if (value == null) {
            settings.remove(key)
            return
        }
        val jsonString = json.encodeToString(value)
        settings.putString(key, jsonString)
    }

    inline fun <reified T> getObject(
        key: String
    ): T? {
        val jsonString = settings.getString(key, "")

        if (jsonString.isEmpty()) return null

        return try {
            json.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    fun clear() {
        settings.clear()
    }
}