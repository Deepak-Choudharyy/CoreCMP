package com.corecmp.shared.security

import android.app.Activity
import android.view.WindowManager
import com.corecmp.shared.api.appContext

private var screenshotBlockingEnabled = false

actual fun setScreenshotBlocking(enabled: Boolean) {
    screenshotBlockingEnabled = enabled
    val activity = appContext as? Activity ?: return
    activity.runOnUiThread {
        if (enabled) {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE,
            )
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

actual fun isScreenshotBlockingEnabled(): Boolean = screenshotBlockingEnabled
