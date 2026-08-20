package com.corecmp.shared.platform

expect fun openMaps(query: String)
expect fun openDialer(phoneNumber: String)
expect fun openWhatsApp(phoneNumber: String, message: String = "")
expect fun openEmail(address: String, subject: String = "", body: String = "")
