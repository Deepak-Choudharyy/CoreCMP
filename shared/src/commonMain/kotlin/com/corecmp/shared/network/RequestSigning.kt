package com.corecmp.shared.network

fun interface RequestSigner {
    fun sign(payload: String, timestamp: Long): String
}

fun signedHeaders(
    signer: RequestSigner,
    payload: String,
    timestamp: Long = kotlin.time.Clock.System.now().toEpochMilliseconds(),
): Map<String, String> = mapOf(
    "X-Timestamp" to timestamp.toString(),
    "X-Signature" to signer.sign(payload, timestamp),
)
