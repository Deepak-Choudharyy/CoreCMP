package com.corecmp.shared.picker

import androidx.compose.runtime.Composable
import com.corecmp.shared.ui.AppSnackbarManager
import com.github.sarxos.webcam.Webcam
import java.awt.BorderLayout
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter

actual class PlatformMediaPicker actual constructor() {

    @Composable
    actual fun RegisterLaunchers() {}

    actual fun launch(
        type: PickerType,
        documentConfig: DocumentConfig?,
        onResult: (PickedFile?) -> Unit
    ){
        try{
            when(type){
                PickerType.CAMERA ->
                    openDesktopCamera(onResult)

                PickerType.IMAGE ->
                    openFileChooser(
                        PickerType.IMAGE,
                        onResult
                    )

                PickerType.DOCUMENT ->
                    openFileChooser(
                        PickerType.DOCUMENT,
                        onResult
                    )
            }
        }
        catch(e: Exception){
            e.printStackTrace()
            AppSnackbarManager.show(
                "Unexpected error"
            )
            onResult(null)
        }
    }
}

private fun openFileChooser(
    type: PickerType,
    onResult: (PickedFile?) -> Unit
) {
    try {
        val chooser = JFileChooser()
        chooser.isMultiSelectionEnabled = false
        chooser.fileSelectionMode = JFileChooser.FILES_ONLY
        chooser.isAcceptAllFileFilterUsed = false

        val home = System.getProperty("user.home")
        val startDir = when(type){
            PickerType.IMAGE -> File(home, "Pictures")
            PickerType.DOCUMENT -> File(home, "Documents")
            else -> File(home)
        }

        if(startDir.exists()){
            chooser.currentDirectory = startDir
        }

        chooser.fileFilter = when(type){
            PickerType.IMAGE ->
                FileNameExtensionFilter(
                    "Images",
                    "jpg",
                    "jpeg",
                    "png",
                    "webp"
                )

            PickerType.DOCUMENT ->
                FileNameExtensionFilter(
                    "Documents",
                    "pdf",
                    "doc",
                    "docx"
                )

            else -> null
        }

        val result = chooser.showOpenDialog(null)

        if(result == JFileChooser.APPROVE_OPTION){
            val file = chooser.selectedFile

            if(!file.exists()){
                AppSnackbarManager.show("File not found")
                onResult(null)
                return
            }

            val bytes = file.readBytes()

            onResult(
                PickedFile(
                    bytes = bytes,
                    fileName = file.name,
                    mimeType = guessMime(file.extension)
                )
            )
        }
        else{
            onResult(null)
        }
    }
    catch (e: Exception){
        e.printStackTrace()
        AppSnackbarManager.show(
            "Unable to open file"
        )
        onResult(null)
    }
}

private fun openDesktopCamera(
    onResult: (PickedFile?) -> Unit
) {
    Thread {
        try {
            val webcam = Webcam.getDefault()
            if (webcam == null) {
                AppSnackbarManager.show("No camera found")
                onResult(null)
                return@Thread
            }

            webcam.open()

            val preview = JLabel()
            val captureBtn = JButton("Capture")
            val cancelBtn = JButton("Cancel")
            val panel = JPanel(BorderLayout())

            panel.add(preview, BorderLayout.CENTER)
            val buttons = JPanel()
            buttons.add(captureBtn)
            buttons.add(cancelBtn)
            panel.add(buttons, BorderLayout.SOUTH)

            val window = JFrame("Camera")
            window.contentPane = panel
            window.setSize(640, 520)
            window.setLocationRelativeTo(null)
            window.isVisible = true

            var running = true

            Thread {
                while(running && webcam.isOpen) {
                    val image = webcam.image
                    if(image != null){
                        SwingUtilities.invokeLater {
                            preview.icon = ImageIcon(image)
                        }
                    }
                    Thread.sleep(30)
                }
            }.start()

            captureBtn.addActionListener {
                try {
                    val image = webcam.image
                    if (image != null) {
                        val baos = ByteArrayOutputStream()
                        ImageIO.write(image, "jpg", baos)
                        val bytes = baos.toByteArray()

                        running = false
                        webcam.close()
                        window.dispose()

                        onResult(
                            PickedFile(
                                bytes = bytes,
                                fileName = "capture.jpg",
                                mimeType = "image/jpeg"
                            )
                        )
                    } else {
                        throw Exception("Captured frame is null")
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                    AppSnackbarManager.show(
                        "Camera capture failed"
                    )
                    onResult(null)
                }
            }

            cancelBtn.addActionListener {
                running = false
                webcam.close()
                window.dispose()
                onResult(null)
            }

            window.addWindowListener(object : java.awt.event.WindowAdapter() {
                override fun windowClosing(e: java.awt.event.WindowEvent?) {
                    running = false
                    webcam.close()
                    onResult(null)
                }
            })

        }
        catch(e: Exception){
            e.printStackTrace()
            AppSnackbarManager.show(
                "Camera not available"
            )
            onResult(null)
        }
    }.start()
}

private fun guessMime(ext: String): String {
    return when (ext.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "pdf" -> "application/pdf"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        else -> "application/octet-stream"
    }
}