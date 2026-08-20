package com.corecmp.shared.update

import com.corecmp.shared.api.HttpClientProvider
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable

@Serializable
data class RemoteVersionConfig(
    val latestVersion: String,
    val minVersion: String = "",
    val updateUrl: String = "",
    val releaseNotes: String = "",
    val forceUpdate: Boolean = false,
)

data class UpdateResult(
    val updateAvailable: Boolean,
    val forceUpdate: Boolean,
    val config: RemoteVersionConfig?,
)

class UpdateChecker {
    suspend fun checkRemote(
        configUrl: String,
        currentVersion: String,
    ): UpdateResult {
        val body = HttpClientProvider.client.get(configUrl).bodyAsText()
        val config = com.corecmp.shared.api.json.decodeFromString<RemoteVersionConfig>(body)
        val needsUpdate = compareVersions(currentVersion, config.latestVersion) < 0
        val force = config.forceUpdate ||
            (config.minVersion.isNotBlank() && compareVersions(currentVersion, config.minVersion) < 0)
        return UpdateResult(
            updateAvailable = needsUpdate,
            forceUpdate = force,
            config = config,
        )
    }

    fun compareVersions(current: String, target: String): Int {
        val currentParts = current.split('.', '-', '_').mapNotNull { it.toIntOrNull() }
        val targetParts = target.split('.', '-', '_').mapNotNull { it.toIntOrNull() }
        val max = maxOf(currentParts.size, targetParts.size)
        for (i in 0 until max) {
            val c = currentParts.getOrElse(i) { 0 }
            val t = targetParts.getOrElse(i) { 0 }
            if (c != t) return c.compareTo(t)
        }
        return 0
    }
}

expect fun openAppUpdate(url: String)

expect fun triggerNativeInAppUpdate()
