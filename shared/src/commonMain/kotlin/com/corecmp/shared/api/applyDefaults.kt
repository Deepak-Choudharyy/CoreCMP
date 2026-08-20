package com.corecmp.shared.api

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders

fun HttpRequestBuilder.applyDefaults(
    baseName: String = ApiConfig.DEFAULT_BASE_NAME
) {
    val config = ApiConfig.getConfig(baseName)

    config.defaultHeaders.forEach { entry ->
        header(entry.key, entry.value)
    }

    config.defaultQueryParams.forEach { entry ->
        parameter(entry.key, entry.value)
    }

    val activeToken = config.tokenProvider?.invoke() ?: config.token
    activeToken?.let { token ->
        val authValue = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
        header(HttpHeaders.Authorization, authValue)
    }
}

fun buildUrl(
    baseName: String = ApiConfig.DEFAULT_BASE_NAME,
    endpoint: String
): String {
    if (endpoint.startsWith("http://", ignoreCase = true) || endpoint.startsWith("https://", ignoreCase = true)) {
        return endpoint
    }

    val config = ApiConfig.getConfig(baseName)
    val cleanBase = config.baseUrl.trimEnd('/')
    val cleanEndpoint = endpoint.trimStart('/')

    return if (cleanBase.isEmpty()) cleanEndpoint else "$cleanBase/$cleanEndpoint"
}