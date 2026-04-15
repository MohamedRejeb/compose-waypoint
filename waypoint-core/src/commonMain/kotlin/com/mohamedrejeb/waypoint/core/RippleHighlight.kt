package com.mohamedrejeb.waypoint.core

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity

/**
 * Renders expanding concentric rings radiating from each target center.
 * Supports multiple targets and both stroke and filled rendering.
 */
@Composable
internal fun RippleHighlight(
    targetBounds: Rect,
    additionalBounds: List<Rect>,
    style: HighlightStyle.Ripple,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val maxRadiusPx = with(density) { style.maxRadius.toPx() }

    val allCenters = buildList {
        add(targetBounds.center)
        additionalBounds.forEach { add(it.center) }
    }

    val infiniteTransition = rememberInfiniteTransition()

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = style.durationMillis),
        ),
    )

    Canvas(modifier = modifier) {
        for (center in allCenters) {
            for (i in 0 until style.ringCount) {
                val ringProgress = (progress + i.toFloat() / style.ringCount) % 1f
                val radius = maxRadiusPx * ringProgress
                val alpha = (1f - ringProgress).coerceIn(0f, 1f) * 0.6f

                if (alpha > 0.01f) {
                    drawCircle(
                        color = style.color.copy(alpha = alpha),
                        center = center,
                        radius = radius,
                        style = if (style.filled) Fill else Stroke(width = 2f),
                    )
                }
            }
        }
    }
}
