package com.corecmp.shared.ui

import android.content.Context
import coil3.PlatformContext

internal actual fun imageCacheDirectory(context: PlatformContext): String {
    val androidContext = context as Context
    return "${androidContext.cacheDir.absolutePath}/image_cache"
}
