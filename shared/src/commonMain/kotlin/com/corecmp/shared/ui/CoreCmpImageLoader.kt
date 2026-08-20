package com.corecmp.shared.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import kotlin.concurrent.Volatile
import kotlin.math.min

/** Shared Coil [ImageLoader] with coreCmp disk + memory caching. */
object CoreCmpImageLoader {
    private const val DISK_CACHE_MAX_BYTES = 50L * 1024L * 1024L
    private const val MEMORY_CACHE_MAX_BYTES = 25L * 1024L * 1024L

    @Volatile
    private var sharedLoader: ImageLoader? = null

    fun create(context: PlatformContext): ImageLoader {
        val cacheDir = imageCacheDirectory(context).toPath()
        if (!FileSystem.SYSTEM.exists(cacheDir)) {
            FileSystem.SYSTEM.createDirectories(cacheDir)
        }

        return ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
                add(SvgDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizeBytes(MEMORY_CACHE_MAX_BYTES)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir)
                    .maxSizeBytes(DISK_CACHE_MAX_BYTES)
                    .build()
            }
            // Coil 3.x already ignores Cache-Control headers by default and always
            // writes responses to the disk cache, so S3's no-store / short max-age
            // headers don't stop images from surviving app restarts. Combined with the
            // stable cache key (query-param stripping), an image downloaded once is
            // served from cache on every subsequent load.
            .build()
    }

    fun get(context: PlatformContext): ImageLoader {
        sharedLoader?.let { return it }
        return create(context).also { loader ->
            sharedLoader = loader
            SingletonImageLoader.setSafe { ctx ->
                sharedLoader ?: create(ctx).also { sharedLoader = it }
            }
        }
    }

    @Composable
    fun remember(context: PlatformContext = LocalPlatformContext.current): ImageLoader {
        return remember { get(context) }
    }

    fun urlRequest(
        context: PlatformContext,
        url: String,
        cacheKey: String = normalizeCacheKey(url),
    ): ImageRequest {
        val cleanUrl = url.trim()
        val builder = ImageRequest.Builder(context)
            .data(cleanUrl)
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .crossfade(false)
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)

        if (isSvgSource(cleanUrl)) {
            builder.decoderFactory(SvgDecoder.Factory())
        }
        return builder.build()
    }

    fun bytesRequest(
        context: PlatformContext,
        bytes: ByteArray,
        cacheKey: String,
        sourcePath: String? = null,
    ): ImageRequest {
        val builder = ImageRequest.Builder(context)
            .data(bytes)
            .memoryCacheKey(cacheKey)
            .diskCacheKey(cacheKey)
            .crossfade(false)
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)

        if (isSvgSource(sourcePath) || isSvgBytes(bytes)) {
            builder.decoderFactory(SvgDecoder.Factory())
        }
        return builder.build()
    }

    suspend fun executeUrl(
        context: PlatformContext,
        url: String,
        cacheKey: String = normalizeCacheKey(url),
    ): ImageResult = get(context).execute(urlRequest(context, url, cacheKey))

    /**
     * AWS S3 presigned-URL parameters (SigV4). These are pure authentication/expiry
     * values that change on every request but never identify the image itself, so
     * they are safe to drop from the cache key. Only stripped when the URL is a real
     * presigned URL (see [normalizeCacheKey]) so we never touch app-specific params
     * that might identify distinct images.
     */
    private val AWS_PRESIGN_PARAMS = setOf(
        "x-amz-algorithm",
        "x-amz-credential",
        "x-amz-date",
        "x-amz-expires",
        "x-amz-signedheaders",
        "x-amz-signature",
        "x-amz-security-token",
        "x-amz-content-sha256",
    )

    /**
     * Builds a stable cache key for an image source.
     *
     * For AWS S3 presigned URLs the object path already uniquely identifies the image,
     * so only the rotating `X-Amz-*` signing params are dropped — the same image then
     * maps to the same key across signature refreshes and app restarts.
     *
     * Every other URL keeps its full query untouched, so two different images can never
     * collapse onto the same cache key (which would show the wrong image). The actual
     * network request always uses the full original URL via [urlRequest].
     */
    fun normalizeCacheKey(value: String): String {
        val cleaned = value.trim().replace("\\/", "/")
        val queryIndex = cleaned.indexOf('?')
        if (queryIndex < 0) return cleaned

        val base = cleaned.substring(0, queryIndex)
        val query = cleaned.substring(queryIndex + 1)
        if (query.isEmpty()) return base

        val params = query.split('&').filter { it.isNotEmpty() }
        val isAwsPresigned = params.any { it.substringBefore('=').lowercase() == "x-amz-signature" }
        if (!isAwsPresigned) return cleaned

        val stableParams = params.filter { param ->
            param.substringBefore('=').lowercase() !in AWS_PRESIGN_PARAMS
        }
        return if (stableParams.isEmpty()) base else base + "?" + stableParams.joinToString("&")
    }

    private fun isSvgSource(path: String?): Boolean =
        path?.endsWith(".svg", ignoreCase = true) == true

    private fun isSvgBytes(bytes: ByteArray): Boolean {
        if (bytes.isEmpty()) return false
        val prefix = bytes.decodeToString(0, min(bytes.size, 256))
        return prefix.contains("<svg", ignoreCase = true)
    }
}
