package com.corecmp.shared.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class CoreCmpTypography(
    val chip: TextStyle = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.2.sp,
    ),
    val amount: TextStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp,
    ),
    val keyLabel: TextStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
    ),
    val valueText: TextStyle = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
    ),
    val banner: TextStyle = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp,
    ),
    val emptyTitle: TextStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    val emptySubtitle: TextStyle = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp,
    ),
)

val LocalCoreCmpTypography = compositionLocalOf { CoreCmpTypography() }

fun CoreCmpTypography.toMaterialTypography(base: Typography = Typography()): Typography = base.copy(
    bodySmall = base.bodySmall.merge(chip),
    titleMedium = base.titleMedium.merge(emptyTitle),
    bodyMedium = base.bodyMedium.merge(valueText),
)
