package com.corecmp.shared.ui.kit

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.corecmp.shared.CoreCmp
import com.corecmp.shared.version.CoreCmpVersionCatalog

@Composable
fun CoreCmpVersionLabel(
    modifier: Modifier = Modifier,
    manifestUrl: String = CoreCmpVersionCatalog.DEFAULT_MANIFEST_URL,
    showLatestHint: Boolean = true,
) {
    var latest by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(manifestUrl) {
        latest = CoreCmpVersionCatalog.fetchManifest(manifestUrl)?.latest
    }

    val text = when {
        !showLatestHint || latest == null || latest == CoreCmp.VERSION ->
            "CoreCmp ${CoreCmp.VERSION}"
        else ->
            "CoreCmp ${CoreCmp.VERSION} · latest $latest"
    }

    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}
