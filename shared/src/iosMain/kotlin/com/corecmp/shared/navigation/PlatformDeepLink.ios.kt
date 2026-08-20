package com.corecmp.shared.navigation

actual fun readPlatformDeepLink(): String? = PlatformDeepLinkStore.pending

actual fun consumePlatformDeepLink() {
    PlatformDeepLinkStore.pending = null
}
