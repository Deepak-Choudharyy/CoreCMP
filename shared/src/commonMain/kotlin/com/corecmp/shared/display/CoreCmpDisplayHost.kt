package com.corecmp.shared.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.corecmp.shared.CoreCmp
import com.corecmp.shared.ui.CustomCheckbox

/**
 * Wrap your app root (or a screen subtree) to apply [DisplaySettingsManager] density overrides.
 *
 * ```
 * CoreCmpDisplayHost {
 *     App()
 * }
 * ```
 */
@Composable
fun CoreCmpDisplayHost(
    manager: DisplaySettingsManager = CoreCmp.display,
    content: @Composable () -> Unit,
) {
    val settings = manager.settings
    val systemDensity = LocalDensity.current

    val effectiveDensity = remember(systemDensity, settings) {
        Density(
            density = if (settings.lockDisplayDensity) {
                settings.referenceDensity
            } else {
                systemDensity.density
            },
            fontScale = if (settings.lockFontScale) {
                settings.fixedFontScale
            } else {
                systemDensity.fontScale
            },
        )
    }

    CompositionLocalProvider(LocalDensity provides effectiveDensity) {
        content()
    }
}

/**
 * Ready-made settings UI. Show from your app settings screen or a bottom sheet.
 * Fixed layout is **opt-in** — default follows the system (store policy friendly).
 */
@Composable
fun DisplaySettingsPanel(
    manager: DisplaySettingsManager = CoreCmp.display,
    modifier: Modifier = Modifier,
) {
    val settings = manager.settings

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Display",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Keep fonts and dialog sizes consistent across devices. " +
                "Turn off to follow your phone's accessibility settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        DisplaySettingRow(
            label = "Fixed font size",
            description = "Ignore system text size (recommended for uniform forms)",
            checked = settings.lockFontScale,
            onCheckedChange = manager::setLockFontScale,
        )

        DisplaySettingRow(
            label = "Fixed display scale",
            description = "Ignore system display size (keeps spacing and dialogs aligned)",
            checked = settings.lockDisplayDensity,
            onCheckedChange = manager::setLockDisplayDensity,
        )

        DisplaySettingRow(
            label = "Uniform layout (both)",
            description = "Lock font size and display scale together",
            checked = settings.lockFontScale && settings.lockDisplayDensity,
            onCheckedChange = { enabled ->
                if (enabled) manager.enableUniformLayout(settings.referenceDensity)
                else manager.useSystemLayout()
            },
        )
    }
}

@Composable
private fun DisplaySettingRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CustomCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
