package com.corecmp.shared.display

import android.content.Context
import android.content.res.Configuration

/**
 * Apply [DisplaySettings] to a [Context] for non-Compose views (WebView, XML, dialogs).
 * Call from `Activity.attachBaseContext` when fixed layout is enabled.
 */
fun Context.withDisplaySettings(settings: DisplaySettings): Context {
    if (!settings.usesFixedLayout) return this

    val config = Configuration(resources.configuration)
    if (settings.lockFontScale) {
        config.fontScale = settings.fixedFontScale
    }
    if (settings.lockDisplayDensity) {
        config.densityDpi = (settings.referenceDensity * 160f).toInt()
    }
    return createConfigurationContext(config)
}
