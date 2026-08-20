package com.corecmp.shared.update

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openAppUpdate(url: String) {
    if (url.isBlank()) return
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

actual fun triggerNativeInAppUpdate() {
    // iOS uses App Store URL via openAppUpdate
}
