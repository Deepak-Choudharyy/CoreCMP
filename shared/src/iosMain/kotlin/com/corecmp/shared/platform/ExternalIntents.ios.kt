package com.corecmp.shared.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openMaps(query: String) {
    val encoded = query.replace(" ", "+")
    val url = NSURL.URLWithString("http://maps.apple.com/?q=$encoded") ?: return
    UIApplication.sharedApplication.openURL(url)
}

actual fun openDialer(phoneNumber: String) {
    val digits = phoneNumber.filter { it.isDigit() || it == '+' }
    val url = NSURL.URLWithString("tel:$digits") ?: return
    UIApplication.sharedApplication.openURL(url)
}

actual fun openWhatsApp(phoneNumber: String, message: String) {
    val digits = phoneNumber.filter { it.isDigit() }
    val path = if (message.isBlank()) {
        "https://wa.me/$digits"
    } else {
        "https://wa.me/$digits?text=${message.replace(" ", "%20")}"
    }
    val url = NSURL.URLWithString(path) ?: return
    UIApplication.sharedApplication.openURL(url)
}

actual fun openEmail(address: String, subject: String, body: String) {
    val url = NSURL.URLWithString(
        "mailto:$address?subject=${subject.replace(" ", "%20")}&body=${body.replace(" ", "%20")}",
    ) ?: return
    UIApplication.sharedApplication.openURL(url)
}
