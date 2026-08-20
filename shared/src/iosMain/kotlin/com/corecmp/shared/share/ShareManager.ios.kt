package com.corecmp.shared.share

import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.Foundation.writeToURL
import platform.Foundation.NSURL
import platform.Foundation.NSTemporaryDirectory
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

actual class ShareManager actual constructor() {

    actual fun shareText(text: String) {
        val rootViewController = com.corecmp.shared.util.topViewController() ?: return
        val activityController = UIActivityViewController(listOf(text), null)
        rootViewController.presentViewController(activityController, true, null)
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual fun shareFile(bytes: ByteArray, fileName: String, mimeType: String) {
        val rootViewController = com.corecmp.shared.util.topViewController() ?: return

        val nsData = bytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = bytes.size.toULong()
            )
        }
        
        val tempDir = NSTemporaryDirectory()
        val fileUrl = NSURL.fileURLWithPath(tempDir + fileName)
        
        if (nsData.writeToURL(fileUrl, true)) {
            val activityController = UIActivityViewController(listOf(fileUrl), null)
            rootViewController.presentViewController(activityController, true, null)
        }
    }
}
