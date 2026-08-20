package com.corecmp.shared.share

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.corecmp.shared.api.appContext
import java.io.File

actual class ShareManager actual constructor() {

    actual fun shareText(text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun shareFile(bytes: ByteArray, fileName: String, mimeType: String) {
        try {
            val cacheFile = File(appContext.cacheDir, fileName).apply {
                writeBytes(bytes)
            }
            val uri: Uri = FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.provider",
                cacheFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share File").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
