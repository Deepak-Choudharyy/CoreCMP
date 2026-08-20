package com.corecmp.shared.version

import com.corecmp.shared.CoreCmp
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CoreCmpRelease(
    val version: String,
    val message: String = "",
    val publishedAt: String = "",
    val commit: String = "",
)

@Serializable
data class CoreCmpVersionManifest(
    val latest: String,
    val versions: List<String> = emptyList(),
    val releases: List<CoreCmpRelease> = emptyList(),
    val updatedAt: String = "",
)

object CoreCmpVersionCatalog {
    /** Hosted Maven repo (GitHub Pages) — JitPack-style version picker. */
    const val MAVEN_REPOSITORY_URL = "https://deepakchoudhary.github.io/CoreCMP/"

    const val GROUP_ID = "com.corecmp"
    const val ARTIFACT_ID = "shared"

    const val DEFAULT_MANIFEST_URL = "${MAVEN_REPOSITORY_URL}versions.json"

    /** Gradle dependency line for any published version. */
    fun dependencyNotation(version: String): String = "$GROUP_ID:$ARTIFACT_ID:$version"

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchManifest(
        url: String = DEFAULT_MANIFEST_URL,
        client: HttpClient = HttpClient(),
    ): CoreCmpVersionManifest? {
        return try {
            client.use {
                json.decodeFromString(it.get(url).bodyAsText())
            }
        } catch (_: Exception) {
            null
        }
    }

    fun hasUpdate(
        current: String = CoreCmp.VERSION,
        manifest: CoreCmpVersionManifest,
    ): Boolean = manifest.latest.isNotBlank() && manifest.latest != current
}
