package com.corecmp.shared.platform

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

actual class ClipboardManager actual constructor() {
    actual fun copy(text: String, label: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(java.awt.datatransfer.StringSelection(text), null)
    }

    actual fun paste(): String? = try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            clipboard.getData(DataFlavor.stringFlavor) as? String
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }

    actual fun hasText(): Boolean = try {
        Toolkit.getDefaultToolkit().systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)
    } catch (_: Exception) {
        false
    }
}
