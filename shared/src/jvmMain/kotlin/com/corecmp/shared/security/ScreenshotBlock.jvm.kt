package com.corecmp.shared.security

private var screenshotBlockingEnabled = false

actual fun setScreenshotBlocking(enabled: Boolean) {
    screenshotBlockingEnabled = enabled
}

actual fun isScreenshotBlockingEnabled(): Boolean = screenshotBlockingEnabled
