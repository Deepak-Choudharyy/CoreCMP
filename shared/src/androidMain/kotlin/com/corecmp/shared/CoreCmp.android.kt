package com.corecmp.shared

import android.content.Context
import com.corecmp.shared.api.initCoreCmp
import com.corecmp.shared.api.appContext
import com.corecmp.shared.api.isAppContextInitialized

internal actual fun platformInit(context: Any?, settingsName: String) {
    if (context is Context) {
        initCoreCmp(context, settingsName)
    }
}

internal actual fun getCacheDir(): String {
    check(isAppContextInitialized()) {
        "Call CoreCmp.init(context) in Application.onCreate() before using cache APIs on Android."
    }
    return appContext.cacheDir.absolutePath + "/image_cache"
}
