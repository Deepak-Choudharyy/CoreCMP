package com.corecmp.shared.update

import java.awt.Desktop
import java.net.URI

actual fun openAppUpdate(url: String) {
    if (url.isBlank() || !Desktop.isDesktopSupported()) return
    runCatching {
        Desktop.getDesktop().browse(URI(url))
    }
}

actual fun triggerNativeInAppUpdate() = Unit
