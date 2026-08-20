package com.corecmp.shared.display

/**
 * Controls how the app responds to system font-size and display-size settings.
 *
 * Defaults respect the OS (Play Store / App Store accessibility friendly).
 * Host apps can let users opt in to a fixed layout for consistent UI across devices.
 */
data class DisplaySettings(
    val lockFontScale: Boolean = false,
    val lockDisplayDensity: Boolean = false,
    val fixedFontScale: Float = 1f,
    /** Compose density baseline (~480 dpi). Used when [lockDisplayDensity] is true. */
    val referenceDensity: Float = 3f,
) {
    val usesFixedLayout: Boolean
        get() = lockFontScale || lockDisplayDensity
}
