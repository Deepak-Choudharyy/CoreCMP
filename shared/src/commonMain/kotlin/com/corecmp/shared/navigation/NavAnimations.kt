package com.corecmp.shared.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

object NavAnimations {
    val slide: NavAnimationPreset = NavAnimationPreset(
        enter = { slideInHorizontally(tween(300)) { it } },
        exit = { slideOutHorizontally(tween(300)) { -it } },
        popEnter = { slideInHorizontally(tween(300)) { -it } },
        popExit = { slideOutHorizontally(tween(300)) { it } },
    )

    val fade: NavAnimationPreset = NavAnimationPreset(
        enter = { fadeIn(tween(250)) },
        exit = { fadeOut(tween(250)) },
        popEnter = { fadeIn(tween(250)) },
        popExit = { fadeOut(tween(250)) },
    )
}

data class NavAnimationPreset(
    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition,
    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition,
    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition,
    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition,
)
