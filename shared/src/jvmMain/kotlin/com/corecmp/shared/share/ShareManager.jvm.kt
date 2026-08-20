package com.corecmp.shared.share

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

actual class ShareManager actual constructor() {

    actual fun shareText(text: String) {
        try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val selection = StringSelection(text)
            clipboard.setContents(selection, null)
            println("Shared text copied to system clipboard: $text")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun shareFile(bytes: ByteArray, fileName: String, mimeType: String) {
        SwingUtilities.invokeLater {
            try {
                val chooser = JFileChooser().apply {
                    dialogTitle = "Save Shared File"
                    selectedFile = File(fileName)
                }
                val result = chooser.showSaveDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    val targetFile = chooser.selectedFile
                    targetFile.writeBytes(bytes)
                    println("Shared file saved successfully: ${targetFile.absolutePath}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
