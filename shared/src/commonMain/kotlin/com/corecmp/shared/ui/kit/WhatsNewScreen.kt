package com.corecmp.shared.ui.kit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.corecmp.shared.ui.CommonButton

data class ChangelogItem(
    val version: String,
    val notes: List<String>,
)

@Composable
fun WhatsNewScreen(
    items: List<ChangelogItem>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("What's New", style = MaterialTheme.typography.headlineSmall)
        items.forEach { item ->
            Text("v${item.version}", style = MaterialTheme.typography.titleMedium)
            item.notes.forEach { note ->
                Text("• $note", style = MaterialTheme.typography.bodyMedium)
            }
        }
        CommonButton(label = "Got it", onClick = onDismiss)
    }
}
