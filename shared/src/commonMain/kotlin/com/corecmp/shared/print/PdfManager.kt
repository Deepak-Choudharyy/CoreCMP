package com.corecmp.shared.print

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPdfManager(): PdfManager

interface PdfManager {
    fun generateAndShare(
        fileName: String,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        content : @Composable () -> Unit
    )
    fun generateAndDownload(
        fileName: String,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        content : @Composable () -> Unit
    )

}