package com.corecmp.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.corecmp.shared.CoreCmp


@Composable
fun CustomLoading(
    loading: Placeholder = CoreCmp.defaultApiLoadingPlaceholder,
    dismissOnBackPress: Boolean = false,
    onDismiss: () -> Unit = {}
) {
    Dialog(onDismissRequest = { if (dismissOnBackPress) onDismiss() }) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.White, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {

            CustomImage(
                placeholder = loading,
                model = null
            )
        }
    }
}

@Composable
fun CustomLoading(
    loadingPathOrUrl: String,
    dismissOnBackPress: Boolean = false,
    onDismiss: () -> Unit = {}
) {
    CustomLoading(
        loading = Placeholder.from(loadingPathOrUrl) ?: CoreCmp.defaultApiLoadingPlaceholder,
        dismissOnBackPress = dismissOnBackPress,
        onDismiss = onDismiss
    )
}

suspend fun loadJson(path: String): String? {

    return CustomImageResourceResolver
        .resolveBytes
        ?.invoke(path)
        ?.decodeToString()

}