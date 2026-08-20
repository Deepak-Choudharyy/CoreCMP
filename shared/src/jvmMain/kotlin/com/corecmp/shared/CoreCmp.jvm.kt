package com.corecmp.shared

internal actual fun platformInit(context: Any?, settingsName: String) {
    // No context initialization required for JVM
}

internal actual fun getCacheDir(): String {
    val tmp = System.getProperty("java.io.tmpdir")
    return "$tmp/corecmp_image_cache"
}
