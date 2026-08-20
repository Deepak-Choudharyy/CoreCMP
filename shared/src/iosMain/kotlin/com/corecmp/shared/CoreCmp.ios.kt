package com.corecmp.shared

import com.corecmp.shared.api.SETTINGS_NAME

internal actual fun platformInit(context: Any?, settingsName: String) {
    SETTINGS_NAME = settingsName
}

internal actual fun getCacheDir(): String {
    val fm = platform.Foundation.NSFileManager.defaultManager
    val cacheUrl = fm.URLsForDirectory(platform.Foundation.NSCachesDirectory, platform.Foundation.NSUserDomainMask).first() as? platform.Foundation.NSURL
    return (cacheUrl?.path ?: "") + "/image_cache"
}
