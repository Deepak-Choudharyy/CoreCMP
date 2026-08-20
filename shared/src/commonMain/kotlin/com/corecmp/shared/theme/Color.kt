package com.corecmp.shared.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val blackColor = Color.Black
val rejectedRedColor = Color(0xFFFF5160)
val whiteColor = Color.White
val transparentColor = Color.Transparent
val bottomSheetHeaderBackGround = Color(0xFFE9F4FE)
val grayColor = Color.Gray
val borderBGColor = grayColor.copy(.3f)

var normalBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF0D47A1),
        Color(0xFF1565C0),
        Color(0xFF2979FF),
        Color(0xFF00E5FF),
        Color(0xFF2979FF)
    )
)
val orangeColor = Color(0xFFE28242)
val yellowColor = Color(0xFFEBD10B)
val colorGreenLight = Color(0xFFF54CAF50)

var successBrush = Brush.linearGradient(
    colors = listOf(
        colorGreenLight,
        colorGreenLight,
        whiteColor
    )
)
var errorBrush = Brush.linearGradient(
    colors = listOf(
        rejectedRedColor,
        rejectedRedColor,
        whiteColor
    )
)
var warningBrush = Brush.linearGradient(
    colors = listOf(
        orangeColor,
        yellowColor,
        whiteColor
    )
)