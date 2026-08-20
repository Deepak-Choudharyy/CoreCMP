package com.corecmp.shared.print

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun rememberPdfManager(): PdfManager {
    val context = LocalContext.current
    val compositionContext = rememberCompositionContext()
    val rootView = LocalView.current

    return remember(context, compositionContext, rootView) {
        AndroidPdfManager(
            context = context,
            compositionContext = compositionContext,
            rootView = rootView
        )
    }
}

class AndroidPdfManager(
    private val context: Context,
    private val compositionContext: CompositionContext,
    private val rootView: View
) : PdfManager {

    override fun generateAndShare(
        fileName: String,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        content: @Composable () -> Unit
    ) {
        onStart()

        captureFullComposeView(
            context = context,
            compositionContext = compositionContext,
            rootView = rootView,
            fileName = fileName,
            content = content,
            onComplete = { file ->
                sharePdfFile(context, file)
                onComplete()
            }
        )
    }

    override fun generateAndDownload(
        fileName: String,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        content: @Composable () -> Unit
    ) {
        onStart()

        captureFullComposeView(
            context = context,
            compositionContext = compositionContext,
            rootView = rootView,
            fileName = fileName,
            content = content,
            onComplete = { file ->
                openPdfFile(context, file)
                onComplete()
            }
        )
    }
}


fun openPdfFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    context.startActivity(intent)
}

fun sharePdfFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share Comparison Plan"))
}

fun captureFullComposeView(
    context: Context,
    compositionContext: CompositionContext,
    rootView: View,
    fileName: String,
    onComplete: (File) -> Unit,
    content: @Composable () -> Unit
) {

    val composeView = ComposeView(context).apply {

        setParentCompositionContext(compositionContext)

        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // 🔥 यहीं UI render होगा
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F7FA))
                    .padding(10.dp)
            ) {

                content()
            }
        }
    }

    val parent = rootView as ViewGroup
    parent.addView(composeView)

    composeView.post {
        try {

            composeView.measure(
                View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )

            composeView.layout(
                0,
                0,
                composeView.measuredWidth,
                composeView.measuredHeight
            )

            val bitmap = createBitmap(composeView.measuredWidth, composeView.measuredHeight)

            val canvas = Canvas(bitmap)
            composeView.draw(canvas)

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(
                bitmap.width,
                bitmap.height,
                1
            ).create()

            val page = pdfDocument.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            val safeFileName = fileName.ifBlank { "document.pdf" }
                .substringAfterLast('/')
                .let { if (it.endsWith(".pdf", ignoreCase = true)) it else "$it.pdf" }
            val file = File(
                context.getExternalFilesDir(null),
                safeFileName
            )

            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            parent.removeView(composeView)

            onComplete(file)

        } catch (e: Exception) {
            e.printStackTrace()
            parent.removeView(composeView)
        }
    }
}