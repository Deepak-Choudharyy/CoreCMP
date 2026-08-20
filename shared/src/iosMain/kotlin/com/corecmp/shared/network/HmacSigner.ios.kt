@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.corecmp.shared.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.kCCHmacAlgSHA256
import platform.posix.memcpy

actual fun hmacSha256(secret: ByteArray, message: String): ByteArray {
    val messageBytes = message.encodeToByteArray()
    return memScoped {
        val output = allocArray<UByteVar>(CC_SHA256_DIGEST_LENGTH)
        secret.usePinned { secretPinned ->
            messageBytes.usePinned { messagePinned ->
                CCHmac(
                    kCCHmacAlgSHA256,
                    secretPinned.addressOf(0),
                    secret.size.convert(),
                    messagePinned.addressOf(0),
                    messageBytes.size.convert(),
                    output,
                )
            }
        }
        ByteArray(CC_SHA256_DIGEST_LENGTH).apply {
            usePinned { pinned ->
                memcpy(pinned.addressOf(0), output, CC_SHA256_DIGEST_LENGTH.convert())
            }
        }
    }
}
