package com.corecmp.shared.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.corecmp.shared.CoreCmp
import com.corecmp.shared.display.DisplaySettingsManager
import com.corecmp.shared.display.CoreCmpDisplayHost

@Composable
fun CoreCmpProviders(
    themeManager: ThemeManager = CoreCmp.theme,
    displayManager: DisplaySettingsManager = CoreCmp.display,
    colors: CoreCmpColors = CoreCmpColors(),
    typography: CoreCmpTypography = CoreCmpTypography(),
    materialTypography: Typography? = null,
    content: @Composable () -> Unit,
) {
    CoreCmpDisplayHost(manager = displayManager) {
        CompositionLocalProvider(
            LocalCoreCmpColors provides colors,
            LocalCoreCmpTypography provides typography,
        ) {
            CoreCmpTheme(
                themeManager = themeManager,
                colors = colors,
                typography = typography,
                materialTypography = materialTypography,
                content = content,
            )
        }
    }
}
