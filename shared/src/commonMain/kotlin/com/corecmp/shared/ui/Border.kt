package com.corecmp.shared.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.corecmp.shared.theme.normalBrush

fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp,
    cornerRadius: Dp
) = this.drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}

@Composable
fun Modifier.beamBorder(radius : Int = 10, brush: Brush = normalBrush): Modifier {

    val transition = rememberInfiniteTransition(label = "beamMove")

    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2500,
                easing = LinearEasing
            )
        ),
        label = "progress"
    )

    return this.drawBehind {

        val strokeWidth = 3.dp.toPx()
        val corner = radius.dp.toPx()
        val roundRect = RoundRect(
            rect = Rect(0f, 0f, size.width, size.height),
            cornerRadius = CornerRadius(corner, corner)
        )
        val borderPath = Path().apply {
            addRoundRect(roundRect)
        }

        val pathMeasure = PathMeasure()
        pathMeasure.setPath(borderPath, true)

        val pathLength = pathMeasure.length

        val start = progress * pathLength
        val beamLength = 420f
        val end = start + beamLength

        val beamPath = Path()

        if (end <= pathLength) {

            // normal segment
            pathMeasure.getSegment(
                start,
                end,
                beamPath,
                true
            )

        } else {

            // segment 1: start → end of path
            pathMeasure.getSegment(
                start,
                pathLength,
                beamPath,
                true
            )

            // segment 2: beginning → remaining
            val remaining = end - pathLength

            pathMeasure.getSegment(
                0f,
                remaining,
                beamPath,
                true
            )
        }

        drawPath(
            path = beamPath,
            brush = brush,
            style = Stroke(width = strokeWidth)
        )
    }
}