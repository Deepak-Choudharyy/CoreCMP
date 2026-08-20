package com.corecmp.shared.print

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSMutableData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithCapacity
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIDocumentInteractionController
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPage
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UILayoutPriorityFittingSizeLevel
import platform.UIKit.UILayoutPriorityRequired
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIWindow
import platform.UIKit.addChildViewController
import platform.UIKit.popoverPresentationController
import platform.UIKit.removeFromParentViewController

@Composable
actual fun rememberPdfManager(): PdfManager = remember { IosPdfManager() }

class IosPdfManager : PdfManager {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @OptIn(ExperimentalForeignApi::class)
    override fun generateAndShare(
        fileName: String,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        content: @Composable () -> Unit
    ) {
        onStart()

        val rootVC = com.corecmp.shared.util.topViewController() ?: return

        val controller = ComposeUIViewController {

            Surface(color = Color.White) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(20.dp)
                ) {
                    content()
                }
            }
        }

        val captureWidth = 900.0
        val initialFrame = CGRectMake(0.0, 0.0, captureWidth, 2000.0)
        controller.view.setFrame(initialFrame)

        rootVC.addChildViewController(controller)
        rootVC.view.insertSubview(controller.view, atIndex = 0L)

        scope.launch {
            delay(2500)

            val view = controller.view

            view.setNeedsLayout()
            view.layoutIfNeeded()

            val fittingSize = view.systemLayoutSizeFittingSize(
                targetSize = CGSizeMake(captureWidth, 0.0),
                withHorizontalFittingPriority = UILayoutPriorityRequired,
                verticalFittingPriority = UILayoutPriorityFittingSizeLevel
            )

            var finalHeight = fittingSize.useContents { height }
            if (finalHeight <= 0.0) finalHeight = 2500.0

            val pageRect = CGRectMake(0.0, 0.0, captureWidth, finalHeight)
            view.setFrame(pageRect)
            view.layoutIfNeeded()

            val pdfData = NSMutableData.dataWithCapacity(0uL)!!
            UIGraphicsBeginPDFContextToData(pdfData, pageRect, null)
            UIGraphicsBeginPDFPage()

            val context = UIGraphicsGetCurrentContext()
            if (context != null) {
                view.drawViewHierarchyInRect(view.bounds, afterScreenUpdates = true)
            }

            UIGraphicsEndPDFContext()

            view.removeFromSuperview()
            controller.removeFromParentViewController()

            val safeFileName = fileName.replace(" ", "_")
            val filePath = NSTemporaryDirectory() + "$safeFileName.pdf"

            if (pdfData.writeToFile(filePath, true)) {
                val fileUrl = NSURL.fileURLWithPath(filePath)
                val activityController = UIActivityViewController(listOf(fileUrl), null)

                if (UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad) {
                    activityController.popoverPresentationController()?.sourceView = rootVC.view
                }
                rootVC.presentViewController(activityController, animated = true) {
                    onComplete()
                }
            } else {
                onComplete()
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun generateAndDownload(
        fileName: String,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        content: @Composable () -> Unit
    ) {
        onStart()

        val rootVC = com.corecmp.shared.util.topViewController() ?: return

        val controller = ComposeUIViewController {
            Surface(color = Color.White) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(20.dp)
                ) {
                    content()
                }
            }
        }

        val captureWidth = 900.0
        val initialFrame = CGRectMake(0.0, 0.0, captureWidth, 2000.0)
        controller.view.setFrame(initialFrame)

        rootVC.addChildViewController(controller)
        rootVC.view.insertSubview(controller.view, atIndex = 0L)

        scope.launch {
            delay(2000)

            val view = controller.view

            val fittingSize = view.systemLayoutSizeFittingSize(
                targetSize = CGSizeMake(captureWidth, 0.0),
                withHorizontalFittingPriority = UILayoutPriorityRequired,
                verticalFittingPriority = UILayoutPriorityFittingSizeLevel
            )

            var finalHeight = fittingSize.useContents { height }
            if (finalHeight <= 0.0) finalHeight = 2500.0

            val pageRect = CGRectMake(0.0, 0.0, captureWidth, finalHeight)
            view.setFrame(pageRect)
            view.layoutIfNeeded()

            val pdfData = NSMutableData.dataWithCapacity(0uL)!!
            UIGraphicsBeginPDFContextToData(pdfData, pageRect, null)
            UIGraphicsBeginPDFPage()

            val context = UIGraphicsGetCurrentContext()
            if (context != null) {
                view.drawViewHierarchyInRect(view.bounds, afterScreenUpdates = true)
            }

            UIGraphicsEndPDFContext()

            view.removeFromSuperview()
            controller.removeFromParentViewController()

            val safeFileName = fileName.replace(" ", "_")
            val filePath = NSTemporaryDirectory() + "$safeFileName.pdf"

            if (pdfData.writeToFile(filePath, true)) {

                val fileUrl = NSURL.fileURLWithPath(filePath)

                // 🔥 Proper iOS Preview/Open
                val docController =
                    UIDocumentInteractionController.interactionControllerWithURL(fileUrl)
                docController.delegate = null

                docController.presentPreviewAnimated(true)

                onComplete()
            } else {
                onComplete()
            }
        }
    }

}