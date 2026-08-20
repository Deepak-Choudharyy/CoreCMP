package com.corecmp.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

fun interface TokenRefreshHandler {
    suspend fun refreshToken(baseName: String): String?
}

data class TokenRefreshConfig(
    val baseName: String,
    val handler: TokenRefreshHandler,
    val enabled: Boolean = true,
    val retryOnStatus: Set<Int> = setOf(HttpStatusCode.Unauthorized.value),
)

fun HttpClient.installTokenRefresh(config: TokenRefreshConfig) {
    if (!config.enabled) return

    plugin(HttpSend).intercept { request ->
        val originalCall = execute(request)
        if (originalCall.response.status.value !in config.retryOnStatus) {
            return@intercept originalCall
        }

        val newToken = config.handler.refreshToken(config.baseName)
        if (newToken.isNullOrBlank()) {
            return@intercept originalCall
        }

        ApiConfig.updateToken(config.baseName, newToken)
        request.applyBearerToken(newToken)
        execute(request)
    }
}

fun HttpClient.installTokenRefresh(
    baseName: String,
    handler: TokenRefreshHandler,
    enabled: Boolean = true,
) {
    installTokenRefresh(
        TokenRefreshConfig(
            baseName = baseName,
            handler = handler,
            enabled = enabled,
        ),
    )
}

fun HttpClientProvider.installTokenRefresh(config: TokenRefreshConfig) {
    client.installTokenRefresh(config)
}

private fun HttpRequestBuilder.applyBearerToken(token: String) {
    headers.remove(HttpHeaders.Authorization)
    header(HttpHeaders.Authorization, "Bearer $token")
}
