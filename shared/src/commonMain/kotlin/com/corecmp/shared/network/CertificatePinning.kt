package com.corecmp.shared.network

/**
 * Host apps pass pins to platform HttpClient builders.
 * Android: OkHttp CertificatePinner; iOS: NSURLSession pinning.
 */
data class CertificatePinConfig(
    val host: String,
    val pins: List<String>,
    val includeSubdomains: Boolean = false,
)

expect fun applyCertificatePinning(config: CertificatePinConfig?)
