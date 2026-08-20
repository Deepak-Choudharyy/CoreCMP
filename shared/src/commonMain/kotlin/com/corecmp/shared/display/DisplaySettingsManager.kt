package com.corecmp.shared.display

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.corecmp.shared.storage.SecureStorage

private const val KEY_LOCK_FONT = "corecmp_cmp_display_lock_font"
private const val KEY_LOCK_DENSITY = "corecmp_cmp_display_lock_density"
private const val KEY_FIXED_FONT_SCALE = "corecmp_cmp_display_fixed_font_scale"
private const val KEY_REFERENCE_DENSITY = "corecmp_cmp_display_reference_density"

class DisplaySettingsManager internal constructor(
    private val storage: SecureStorage = SecureStorage(),
) {
    var settings by mutableStateOf(loadSettings())
        private set

    fun setLockFontScale(enabled: Boolean) {
        update(settings.copy(lockFontScale = enabled))
    }

    fun setLockDisplayDensity(enabled: Boolean) {
        update(settings.copy(lockDisplayDensity = enabled))
    }

    /** Locks font scale and display density so dialogs, text, and spacing look the same on all devices. */
    fun enableUniformLayout(referenceDensity: Float = 3f) {
        update(
            DisplaySettings(
                lockFontScale = true,
                lockDisplayDensity = true,
                fixedFontScale = 1f,
                referenceDensity = referenceDensity,
            )
        )
    }

    /** Restores system font size and display size behaviour. */
    fun useSystemLayout() {
        update(DisplaySettings())
    }

    fun update(newSettings: DisplaySettings) {
        settings = newSettings
        persist(newSettings)
    }

    private fun loadSettings(): DisplaySettings {
        return DisplaySettings(
            lockFontScale = storage.getBoolean(KEY_LOCK_FONT, false),
            lockDisplayDensity = storage.getBoolean(KEY_LOCK_DENSITY, false),
            fixedFontScale = storage.getString(KEY_FIXED_FONT_SCALE, "1").toFloatOrNull() ?: 1f,
            referenceDensity = storage.getString(KEY_REFERENCE_DENSITY, "3").toFloatOrNull() ?: 3f,
        )
    }

    private fun persist(settings: DisplaySettings) {
        storage.putBoolean(KEY_LOCK_FONT, settings.lockFontScale)
        storage.putBoolean(KEY_LOCK_DENSITY, settings.lockDisplayDensity)
        storage.putString(KEY_FIXED_FONT_SCALE, settings.fixedFontScale.toString())
        storage.putString(KEY_REFERENCE_DENSITY, settings.referenceDensity.toString())
    }
}
