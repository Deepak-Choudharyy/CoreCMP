package com.corecmp.shared.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import kotlin.time.Clock

private var lastNavigateAtMs = 0L
private var lastNavigateRoute: String? = null

/**
 * Debounced navigate with [launchSingleTop] to avoid duplicate destinations
 * from rapid taps or recompositions.
 */
fun NavController.navigateOnce(
    route: String,
    debounceMs: Long = 500L,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    val now = Clock.System.now().toEpochMilliseconds()
    if (route == lastNavigateRoute && now - lastNavigateAtMs < debounceMs) return
    lastNavigateAtMs = now
    lastNavigateRoute = route
    navigate(route) {
        launchSingleTop = true
        builder()
    }
}

/** Pop the back stack up to [route], optionally including that destination. */
fun NavController.popUpToRoute(route: String, inclusive: Boolean = false) {
    popBackStack(route, inclusive)
}

/** Alias for [popUpToRoute]; returns whether the pop succeeded. */
fun NavController.popToRoute(route: String, inclusive: Boolean = false): Boolean =
    popBackStack(route, inclusive)

/** Returns route strings for the current back stack (debug helper). */
fun NavController.currentBackStackRoutes(): List<String> =
    currentBackStack.value.mapNotNull { it.destination.route }
