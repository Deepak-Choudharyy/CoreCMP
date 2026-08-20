package com.corecmp.shared.platform

import android.content.Intent
import android.net.Uri
import com.corecmp.shared.api.appContext

actual fun openMaps(query: String) {
    val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
    val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    appContext.startActivity(intent)
}

actual fun openDialer(phoneNumber: String) {
    val digits = phoneNumber.filter { it.isDigit() || it == '+' }
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    appContext.startActivity(intent)
}

actual fun openWhatsApp(phoneNumber: String, message: String) {
    val digits = phoneNumber.filter { it.isDigit() }
    val uri = Uri.parse(
        "https://wa.me/$digits" + if (message.isNotBlank()) "?text=${Uri.encode(message)}" else "",
    )
    val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    appContext.startActivity(intent)
}

actual fun openEmail(address: String, subject: String, body: String) {
    val uri = Uri.parse(
        "mailto:$address?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}",
    )
    val intent = Intent(Intent.ACTION_SENDTO, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    appContext.startActivity(intent)
}
