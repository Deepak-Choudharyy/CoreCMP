package com.corecmp.shared.ui

import coil3.PlatformContext
import com.corecmp.shared.getCacheDir

internal actual fun imageCacheDirectory(context: PlatformContext): String = getCacheDir()
