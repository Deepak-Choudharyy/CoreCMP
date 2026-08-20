package com.corecmp.shared.platform

import java.awt.Desktop
import java.net.URI

actual fun openMaps(query: String) {
    if (!Desktop.isDesktopSupported()) return
    Desktop.getDesktop().browse(URI("https://www.google.com/maps/search/?api=1&query=${query.replace(" ", "+")}"))
}

actual fun openDialer(phoneNumber: String) {
    println("Dialer not available on JVM: $phoneNumber")
}

actual fun openWhatsApp(phoneNumber: String, message: String) {
    if (!Desktop.isDesktopSupported()) return
    val digits = phoneNumber.filter { it.isDigit() }
    val path = if (message.isBlank()) {
        "https://wa.me/$digits"
    } else {
        "https://wa.me/$digits?text=${message.replace(" ", "%20")}"
    }
    Desktop.getDesktop().browse(URI(path))
}

actual fun openEmail(address: String, subject: String, body: String) {
    if (!Desktop.isDesktopSupported()) return
    Desktop.getDesktop().mail(URI("mailto:$address?subject=$subject&body=$body"))
}
