package com.corecmp.shared.network

import com.corecmp.shared.api.HttpClientProvider
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

data class HealthPingResult(
    val isHealthy: Boolean,
    val statusCode: Int?,
    val latency: Duration?,
    val error: String? = null,
)

suspend fun pingApiHealth(
    url: String,
    timeout: Duration = 5.seconds,
): HealthPingResult {
    val mark = TimeSource.Monotonic.markNow()
    return try {
        val response: HttpResponse = withTimeout(timeout) {
            HttpClientProvider.client.get(url)
        }
        HealthPingResult(
            isHealthy = response.status.value in 200..299,
            statusCode = response.status.value,
            latency = mark.elapsedNow(),
        )
    } catch (e: Exception) {
        HealthPingResult(
            isHealthy = false,
            statusCode = null,
            latency = mark.elapsedNow(),
            error = e.message,
        )
    }
}
