package com.corecmp.shared.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * Navigate to [route] only when [condition] is true; otherwise navigate to [fallbackRoute]
 * if provided.
 */
fun NavController.navigateIf(
    condition: Boolean,
    route: String,
    fallbackRoute: String? = null,
    debounceMs: Long = 500L,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    if (condition) {
        navigateOnce(route, debounceMs, builder)
    } else if (fallbackRoute != null) {
        navigateOnce(fallbackRoute, debounceMs, builder)
    }
}

/**
 * Skip navigation when [blocker] is true and invoke [onBlocked] instead.
 */
fun NavController.navigateUnlessBlocked(
    route: String,
    blocker: Boolean,
    onBlocked: () -> Unit = {},
    debounceMs: Long = 500L,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    if (blocker) {
        onBlocked()
    } else {
        navigateOnce(route, debounceMs, builder)
    }
}
