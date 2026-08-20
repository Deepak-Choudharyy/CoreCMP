package com.corecmp.shared.accessibility

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.minTouchTarget(size: Dp = 48.dp): Modifier =
    defaultMinSize(minWidth = size, minHeight = size)
