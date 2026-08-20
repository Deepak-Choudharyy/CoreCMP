package com.corecmp.shared.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class CoreCmpColors(
    val primary: Color = Color(0xFF2196F3),
    val onPrimary: Color = whiteColor,
    val success: Color = Color(0xFF4CAF50),
    val onSuccess: Color = whiteColor,
    val warning: Color = orangeColor,
    val onWarning: Color = blackColor,
    val error: Color = rejectedRedColor,
    val onError: Color = whiteColor,
    val info: Color = Color(0xFF2196F3),
    val onInfo: Color = whiteColor,
    val surfaceMuted: Color = borderBGColor,
    val chipBackground: Color = bottomSheetHeaderBackGround,
    val skeletonBase: Color = grayColor.copy(alpha = 0.15f),
    val skeletonHighlight: Color = grayColor.copy(alpha = 0.35f),
)

val LocalCoreCmpColors = compositionLocalOf { CoreCmpColors() }
