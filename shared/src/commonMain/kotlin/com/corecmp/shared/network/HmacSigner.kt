package com.corecmp.shared.network

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

expect fun hmacSha256(secret: ByteArray, message: String): ByteArray

@OptIn(ExperimentalEncodingApi::class)
class HmacRequestSigner(
    private val secret: ByteArray,
) : RequestSigner {
    override fun sign(payload: String, timestamp: Long): String {
        val message = "$timestamp.$payload"
        return Base64.encode(hmacSha256(secret, message))
    }
}
