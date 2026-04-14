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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity

/**
 * Renders expanding concentric rings radiating from the target center.
 * Rings are staggered so they appear in sequence.
 */
@Composable
internal fun RippleHighlight(
    targetBounds: Rect,
    style: HighlightStyle.Ripple,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val maxRadiusPx = with(density) { style.maxRadius.toPx() }

    val infiniteTransition = rememberInfiniteTransition()

    // Single progress value from 0 → 1, each ring is offset by (1/ringCount)
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = style.durationMillis),
        ),
    )

    Canvas(modifier = modifier) {
        val center = targetBounds.center

        for (i in 0 until style.ringCount) {
            // Stagger each ring
            val ringProgress = (progress + i.toFloat() / style.ringCount) % 1f

            val radius = maxRadiusPx * ringProgress
            val alpha = (1f - ringProgress).coerceIn(0f, 1f) * 0.6f

            if (alpha > 0.01f) {
                drawCircle(
                    color = style.color.copy(alpha = alpha),
                    center = center,
                    radius = radius,
                    style = Stroke(width = 2f),
                )
            }
        }
    }
}
