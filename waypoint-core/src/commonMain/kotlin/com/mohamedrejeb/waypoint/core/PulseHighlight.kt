package com.mohamedrejeb.waypoint.core

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.max

/**
 * Renders an animated pulsing shape around the target element.
 * No dimming overlay -- the shape breathes (scales) to draw attention.
 * Supports both stroke (border) and filled rendering.
 */
@Composable
internal fun PulseHighlight(
    targetBounds: Rect,
    style: HighlightStyle.Pulse,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val borderWidthPx = with(density) { style.borderWidth.toPx() }

    val paddedBounds = with(density) {
        Rect(
            left = targetBounds.left - style.padding.start.toPx(),
            top = targetBounds.top - style.padding.top.toPx(),
            right = targetBounds.right + style.padding.end.toPx(),
            bottom = targetBounds.bottom + style.padding.bottom.toPx(),
        )
    }

    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = style.pulseScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = style.durationMillis / 2),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = style.durationMillis / 2),
            repeatMode = RepeatMode.Reverse,
        ),
    )

    val drawStyle: DrawStyle = if (style.filled) Fill else Stroke(width = borderWidthPx)

    Canvas(modifier = modifier) {
        val center = paddedBounds.center
        val halfWidth = paddedBounds.width / 2f
        val halfHeight = paddedBounds.height / 2f

        // Inner shape (static, full alpha)
        drawShape(
            shape = style.shape,
            bounds = paddedBounds,
            color = style.color,
            drawStyle = drawStyle,
            density = density,
        )

        // Outer shape (animated scale + fading)
        val scaledBounds = Rect(
            left = center.x - halfWidth * scale,
            top = center.y - halfHeight * scale,
            right = center.x + halfWidth * scale,
            bottom = center.y + halfHeight * scale,
        )
        drawShape(
            shape = style.shape,
            bounds = scaledBounds,
            color = style.color.copy(alpha = alpha),
            drawStyle = drawStyle,
            density = density,
        )
    }
}

/**
 * Draws a shape at the given bounds with the specified [DrawStyle] (Fill or Stroke).
 * Shared between PulseHighlight, BorderHighlight, and others.
 */
internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawShape(
    shape: SpotlightShape,
    bounds: Rect,
    color: androidx.compose.ui.graphics.Color,
    drawStyle: DrawStyle,
    density: androidx.compose.ui.unit.Density,
) {
    when (shape) {
        is SpotlightShape.Circle -> {
            val radius = max(bounds.width, bounds.height) / 2f
            drawCircle(
                color = color,
                center = bounds.center,
                radius = radius,
                style = drawStyle,
            )
        }

        is SpotlightShape.Rect -> {
            drawRect(
                color = color,
                topLeft = bounds.topLeft,
                size = bounds.size,
                style = drawStyle,
            )
        }

        is SpotlightShape.RoundedRect -> {
            val cornerRadiusPx = with(density) { shape.cornerRadius.toPx() }
            drawRoundRect(
                color = color,
                topLeft = bounds.topLeft,
                size = bounds.size,
                cornerRadius = CornerRadius(cornerRadiusPx),
                style = drawStyle,
            )
        }

        is SpotlightShape.Pill -> {
            val cornerRadiusPx = bounds.height / 2f
            drawRoundRect(
                color = color,
                topLeft = bounds.topLeft,
                size = bounds.size,
                cornerRadius = CornerRadius(cornerRadiusPx),
                style = drawStyle,
            )
        }
    }
}
