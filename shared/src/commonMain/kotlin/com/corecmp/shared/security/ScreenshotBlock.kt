package com.corecmp.shared.security

/**
 * Enables or disables screenshot / screen-recording capture on sensitive screens.
 * Android uses [android.view.WindowManager.LayoutParams.FLAG_SECURE].
 */
expect fun setScreenshotBlocking(enabled: Boolean)

expect fun isScreenshotBlockingEnabled(): Boolean
