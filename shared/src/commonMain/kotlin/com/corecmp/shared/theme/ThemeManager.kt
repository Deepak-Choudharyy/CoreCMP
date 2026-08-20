package com.corecmp.shared.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.corecmp.shared.storage.SecureStorage

private const val KEY_THEME_MODE = "corecmp_cmp_theme_mode"

class ThemeManager internal constructor(
    private val storage: SecureStorage = SecureStorage(),
) {
    var mode by mutableStateOf(loadMode())
        private set

    fun updateMode(newMode: AppThemeMode) {
        mode = newMode
        storage.putString(KEY_THEME_MODE, newMode.name)
    }

    private fun loadMode(): AppThemeMode {
        return runCatching {
            AppThemeMode.valueOf(storage.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name))
        }.getOrDefault(AppThemeMode.SYSTEM)
    }
}
