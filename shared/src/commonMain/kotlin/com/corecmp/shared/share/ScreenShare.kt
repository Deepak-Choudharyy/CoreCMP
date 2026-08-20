package com.corecmp.shared.share

import androidx.compose.runtime.Composable
import com.corecmp.shared.print.PdfManager

/**
 * Renders [content] to PDF and opens the native share sheet.
 */
fun PdfManager.shareScreenAsPdf(
    fileName: String,
    onStart: () -> Unit = {},
    onComplete: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    generateAndShare(
        fileName = fileName,
        onStart = onStart,
        onComplete = onComplete,
        content = content,
    )
}

/**
 * Share plain text built from a receipt/summary card.
 */
fun ShareManager.shareReceipt(
    title: String,
    lines: List<Pair<String, String>>,
    footer: String = "Shared via CoreCmp",
) {
    val body = buildString {
        appendLine(title)
        appendLine("—".repeat(title.length.coerceAtMost(24)))
        lines.forEach { (label, value) ->
            appendLine("$label: $value")
        }
        appendLine()
        append(footer)
    }
    shareText(body)
}
