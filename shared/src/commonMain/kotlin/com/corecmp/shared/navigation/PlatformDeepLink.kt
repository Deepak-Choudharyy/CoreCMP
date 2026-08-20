package com.corecmp.shared.navigation

/**
 * Reads a platform-provided deep link captured at cold start (Android Intent / iOS activity).
 * Call [consumePlatformDeepLink] after handling to clear the pending value.
 */
expect fun readPlatformDeepLink(): String?

/** Clears the pending platform deep link after it has been routed. */
expect fun consumePlatformDeepLink()

/** Host apps call this when receiving a new deep link (e.g. from Activity intent). */
fun setPlatformDeepLink(uri: String?) {
    PlatformDeepLinkStore.pending = uri
}

internal object PlatformDeepLinkStore {
    var pending: String? = null
}
