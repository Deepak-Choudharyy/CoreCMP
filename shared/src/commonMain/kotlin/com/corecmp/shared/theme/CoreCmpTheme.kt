package com.corecmp.shared.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.corecmp.shared.CoreCmp

@Composable
fun CoreCmpTheme(
    themeManager: ThemeManager = CoreCmp.theme,
    colors: CoreCmpColors = LocalCoreCmpColors.current,
    typography: CoreCmpTypography = LocalCoreCmpTypography.current,
    materialTypography: Typography? = null,
    content: @Composable () -> Unit,
) {
    val mode = themeManager.mode
    val useDark = when (mode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK, AppThemeMode.AMOLED -> true
    }

    val colorScheme = when {
        mode == AppThemeMode.AMOLED -> darkColorScheme(
            primary = colors.primary,
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color(0xFF121212),
            onBackground = Color.White,
            onSurface = Color.White,
            error = colors.error,
        )
        useDark -> darkColorScheme(
            primary = colors.primary,
            error = colors.error,
        )
        else -> lightColorScheme(
            primary = colors.primary,
            error = colors.error,
        )
    }

    val resolvedTypography = if (materialTypography != null) {
        typography.toMaterialTypography(base = materialTypography)
    } else {
        typography.toMaterialTypography()
    }

    CompositionLocalProvider(
        LocalCoreCmpColors provides colors,
        LocalCoreCmpTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = resolvedTypography,
            content = content,
        )
    }
}
