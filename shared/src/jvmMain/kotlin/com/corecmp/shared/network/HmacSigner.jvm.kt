package com.corecmp.shared.network

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

actual fun hmacSha256(secret: ByteArray, message: String): ByteArray {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(secret, "HmacSHA256"))
    return mac.doFinal(message.toByteArray())
}
