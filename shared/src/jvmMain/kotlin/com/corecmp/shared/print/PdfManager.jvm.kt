package com.corecmp.shared.print

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import com.corecmp.shared.theme.whiteColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import java.awt.Desktop
import java.io.File

@Composable
actual fun rememberPdfManager(): PdfManager {
    return remember { DesktopPdfManager() }
}

class DesktopPdfManager : PdfManager {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun generateAndShare(
        fileName: String,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        content: @Composable () -> Unit
    ) {
        // Desktop pe "share" = same as download/open
        generateAndDownload(fileName, onStart, onComplete, content)
    }

    override fun generateAndDownload(
        fileName: String,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        content: @Composable () -> Unit
    ) {
        onStart()

        scope.launch {
            try {
                val bitmap = renderComposableToBitmap(
                    widthPx = 900,
                    content = content
                )

                val file = saveBitmapAsPdf(bitmap, fileName)

                openPdfFile(file)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onComplete()
            }
        }
    }
}


fun saveBitmapAsPdf(bitmap: ImageBitmap, fileName: String): File {
    val awtImage = bitmap.toAwtImage()

    val document = PDDocument()

    val page = PDPage(
        PDRectangle(awtImage.width.toFloat(), awtImage.height.toFloat())
    )
    document.addPage(page)

    val pdImage = LosslessFactory.createFromImage(document, awtImage)

    PDPageContentStream(document, page).use { contentStream ->
        contentStream.drawImage(
            pdImage,
            0f,
            0f,
            awtImage.width.toFloat(),
            awtImage.height.toFloat()
        )
    }

    val safeFileName = fileName.replace(" ", "_")
    val file = File(System.getProperty("java.io.tmpdir"), "$safeFileName.pdf")

    document.save(file)
    document.close()

    return file
}


fun openPdfFile(file: File) {
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(file)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun renderComposableToBitmap(
    widthPx: Int = 900,
    heightPx: Int = 2000,
    content: @Composable () -> Unit
): ImageBitmap {

    // 1. ComposeScene ki jagah ImageComposeScene use karo
    return ImageComposeScene(width = widthPx, height = heightPx).use { scene ->

        // 2. Content set karo
        scene.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize() // fillMaxWidth() ki jagah fillMaxSize() better hai yahan
                    .background(whiteColor)
                    .padding(16.dp)
            ) {
                content()
            }
        }

        // 3. Render method direct ImageBitmap return karta hai!
        // (No need for PictureRecorder, Surface, or Canvas)
        scene.render().toComposeImageBitmap()

    } // .use {} block automatically scene.close() call kar dega taaki memory leak na ho
}