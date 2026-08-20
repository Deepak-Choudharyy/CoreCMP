package com.corecmp.shared.ui

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Scale
import coil3.svg.SvgDecoder
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import com.corecmp.shared.CoreCmp
import com.corecmp.shared.getCacheDir
import coil3.disk.DiskCache
import okio.Path.Companion.toPath



sealed interface Placeholder {
    data class LottieUrl(val url: String) : Placeholder
    data class LottieJson(val json: String) : Placeholder
    data class LottieBytes(val bytes: ByteArray) : Placeholder
    data class PainterResource(val painter: Painter) : Placeholder
    data class VectorResource(val imageVector: ImageVector) : Placeholder
    data class ImageUrl(val url: String) : Placeholder

    companion object {
        fun from(source: Any): Placeholder? {
            return when (source) {
                is String -> {
                    if (source.endsWith(".json", ignoreCase = true)) {
                        if (source.startsWith("http", ignoreCase = true)) LottieUrl(source) else LottieJson(source)
                    } else if (source.startsWith("http", ignoreCase = true)) {
                        ImageUrl(source)
                    } else {
                        ImageUrl(source)
                    }
                }
                is ByteArray -> LottieBytes(source)
                is Painter -> PainterResource(source)
                is ImageVector -> VectorResource(source)
                else -> null
            }
        }
    }
}

@Composable
fun CustomImage(
    model: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholder: Placeholder? = CoreCmp.defaultImagePlaceholder,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        when (model) {
            is Painter -> {
                Image(
                    painter = model,
                    contentDescription = contentDescription,
                    modifier = modifier,
                    alignment = alignment,
                    contentScale = contentScale,
                    alpha = alpha,
                    colorFilter = colorFilter
                )
            }
            is ImageVector -> {
                Image(
                    imageVector = model,
                    contentDescription = contentDescription,
                    modifier = modifier,
                    alignment = alignment,
                    contentScale = contentScale,
                    alpha = alpha,
                    colorFilter = colorFilter
                )
            }
            else -> {
                when (placeholder) {
                    is Placeholder.PainterResource -> {
                        Image(
                            painter = placeholder.painter,
                            contentDescription = null,
                            modifier = modifier,
                            contentScale = contentScale
                        )
                    }
                    is Placeholder.VectorResource -> {
                        Image(
                            imageVector = placeholder.imageVector,
                            contentDescription = null,
                            modifier = modifier,
                            contentScale = contentScale
                        )
                    }
                    else -> {
                        Box(
                            modifier = modifier.background(Color.LightGray.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Preview Loading...",
                                fontSize = 10.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
        return
    }

    val context = LocalPlatformContext.current

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
                add(SvgDecoder.Factory())
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(getCacheDir().toPath())
                    .maxSizeBytes(50L * 1024L * 1024L) // 50MB
                    .build()
            }
            .build()
    }

    val showPlaceholder: @Composable () -> Unit = {

        when (placeholder) {
            is Placeholder.LottieUrl,
            is Placeholder.LottieJson,
            is Placeholder.LottieBytes -> LottiePlaceholder(
                placeholder,
                modifier,
                contentScale
            )

            is Placeholder.PainterResource -> Image(
                painter = placeholder.painter,
                contentDescription = null,
                modifier = modifier,
                contentScale = contentScale
            )

            is Placeholder.VectorResource -> Image(
                imageVector = placeholder.imageVector,
                contentDescription = null,
                modifier = modifier,
                contentScale = contentScale
            )

            is Placeholder.ImageUrl -> AsyncImage(
                model = placeholder.url,
                contentDescription = null,
                modifier = modifier,
                contentScale = contentScale
            )

            null -> {}
        }
    }

    if (model == null) {
        showPlaceholder()
        return
    }

    when (model) {

        is Painter -> Image(
            painter = model,
            contentDescription = contentDescription,
            modifier = modifier,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter
        )

        is ImageVector -> Image(
            imageVector = model,
            contentDescription = contentDescription,
            modifier = modifier,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter
        )

        is ByteArray -> SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(model)
                .decoderFactory(SvgDecoder.Factory())
                .crossfade(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            imageLoader = imageLoader,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            colorFilter = colorFilter
        ) {

            val state by painter.state.collectAsState()
            if (state is AsyncImagePainter.State.Success)
                SubcomposeAsyncImageContent()
            else
                showPlaceholder()
        }

        is String -> {
            val cleanPath = model.trim().replace("\\/", "/")
            val isUrl = cleanPath.startsWith("http")
            val isJson = cleanPath.endsWith(".json", true)
            val looksLikeFile = cleanPath.contains(".")

            if (isJson) {
                if (isUrl) {
                    LottiePlaceholder(Placeholder.LottieUrl(cleanPath), modifier, contentScale)
                } else {
                    LottiePlaceholder(Placeholder.LottieJson(cleanPath), modifier, contentScale)
                }
                return
            }

            if (isUrl) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(cleanPath)
                        .decoderFactory(SvgDecoder.Factory())
                        .memoryCacheKey(cleanPath)
                        .diskCacheKey(cleanPath)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .scale(Scale.FIT)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = contentScale,
                    colorFilter = colorFilter
                ) {
                    val state by painter.state.collectAsState()
                    if (state is AsyncImagePainter.State.Success) SubcomposeAsyncImageContent()
                    else showPlaceholder()
                }
                return
            }

            if (looksLikeFile) {
                val bytes by produceState<ByteArray?>(
                    initialValue = null,
                    key1 = cleanPath
                ) {
                    value = try {
                        CustomImageResourceResolver
                            .resolveBytes
                            ?.invoke(cleanPath)
                    } catch (e: Exception) {
                        println("Image resolve failed: $cleanPath")
                        null
                    }
                }

                if (bytes != null) {
                    val requestBuilder = ImageRequest.Builder(context)
                        .data(bytes)
                        .crossfade(true)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                    // Only force SVG decoder for vector assets; WebP/PNG use platform decoders.
                    if (cleanPath.endsWith(".svg", ignoreCase = true)) {
                        requestBuilder.decoderFactory(SvgDecoder.Factory())
                    }
                    SubcomposeAsyncImage(
                        model = requestBuilder.build(),
                        imageLoader = imageLoader,
                        contentDescription = contentDescription,
                        modifier = modifier,
                        contentScale = contentScale,
                        colorFilter = colorFilter
                    ) {
                        val state by painter.state.collectAsState()
                        if (state is AsyncImagePainter.State.Success)
                            SubcomposeAsyncImageContent()
                        else
                            showPlaceholder()
                    }
                    return
                }
            }
            showPlaceholder()
        }

        else -> showPlaceholder()
    }
}

object CustomImageResourceResolver {
    var resolveBytes: (suspend (String) -> ByteArray?)? = null
}

private object LottiePlaceholderCache {
    private val jsonByKey = mutableMapOf<String, String>()

    suspend fun resolveJson(placeholder: Placeholder): String? {
        return try {
            when (placeholder) {
                is Placeholder.LottieUrl -> {
                    jsonByKey.getOrPut(placeholder.url) {
                        HttpClient().get(placeholder.url).bodyAsText()
                    }
                }

                is Placeholder.LottieJson -> {
                    val pathOrJson = placeholder.json
                    if (pathOrJson.endsWith(".json", ignoreCase = true)) {
                        jsonByKey[pathOrJson] ?: run {
                            val decoded = CustomImageResourceResolver.resolveBytes
                                ?.invoke(pathOrJson)
                                ?.decodeToString()
                            if (decoded != null) {
                                jsonByKey[pathOrJson] = decoded
                            }
                            decoded
                        }
                    } else {
                        pathOrJson
                    }
                }

                is Placeholder.LottieBytes -> placeholder.bytes.decodeToString()
                else -> null
            }
        } catch (e: Exception) {
            println("Lottie error: $e")
            null
        }
    }
}

@Composable
private fun LottiePlaceholderFallback(modifier: Modifier) {
    Box(
        modifier = modifier.background(Color.LightGray.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {}
}

@Composable
fun LottiePlaceholder(
    placeholder: Placeholder,
    modifier: Modifier,
    contentScale: ContentScale
) {
    val jsonString by produceState<String?>(initialValue = null, placeholder) {
        value = LottiePlaceholderCache.resolveJson(placeholder)
    }

    val loadedJson = jsonString
    if (loadedJson.isNullOrBlank()) {
        LottiePlaceholderFallback(modifier)
        return
    }

    key(loadedJson) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.JsonString(loadedJson),
        )

        if (composition == null) {
            LottiePlaceholderFallback(modifier)
        } else {
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = Int.MAX_VALUE,
            )

            Image(
                painter = rememberLottiePainter(
                    composition = composition,
                    progress = { progress },
                ),
                contentDescription = null,
                modifier = modifier,
                contentScale = contentScale,
            )
        }
    }
}
