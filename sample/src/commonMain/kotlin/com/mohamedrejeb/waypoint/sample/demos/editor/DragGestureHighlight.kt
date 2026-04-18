package com.mohamedrejeb.waypoint.sample.demos.editor

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * A custom highlight that suggests a drag-to-resize gesture. Draws a soft
 * rounded-rect ring around [animatedBounds] and a rightward-streaming trail of
 * three dots starting at the right edge of the target. The dots loop forever
 * with staggered phases so they appear to flow outward.
 */
@Composable
internal fun DragGestureHighlight(
    animatedBounds: Rect,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val ringPadPx = with(density) { 6.dp.toPx() }
    val ringStrokePx = with(density) { 2.dp.toPx() }
    val cornerRadiusPx = with(density) { 6.dp.toPx() }
    val dotRadiusPx = with(density) { 4.dp.toPx() }
    val dotTravelPx = with(density) { 36.dp.toPx() }

    val ringColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.primary

    val transition = rememberInfiniteTransition(label = "DragGestureHighlight")
    val progressA by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dotA",
    )
    val progressB by transition.animateFloat(
        initialValue = 0.33f,
        targetValue = 1.33f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dotB",
    )
    val progressC by transition.animateFloat(
        initialValue = 0.66f,
        targetValue = 1.66f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dotC",
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (animatedBounds.width <= 0f || animatedBounds.height <= 0f) return@Canvas

            // Ring around the target
            val ringRect = Rect(
                left = animatedBounds.left - ringPadPx,
                top = animatedBounds.top - ringPadPx,
                right = animatedBounds.right + ringPadPx,
                bottom = animatedBounds.bottom + ringPadPx,
            )
            drawRoundRect(
                color = ringColor.copy(alpha = 0.85f),
                topLeft = Offset(ringRect.left, ringRect.top),
                size = Size(ringRect.width, ringRect.height),
                cornerRadius = CornerRadius(cornerRadiusPx),
                style = Stroke(width = ringStrokePx),
            )

            // Soft outer glow ring
            drawRoundRect(
                color = ringColor.copy(alpha = 0.25f),
                topLeft = Offset(ringRect.left - ringStrokePx, ringRect.top - ringStrokePx),
                size = Size(
                    ringRect.width + ringStrokePx * 2f,
                    ringRect.height + ringStrokePx * 2f,
                ),
                cornerRadius = CornerRadius(cornerRadiusPx + ringStrokePx),
                style = Stroke(width = ringStrokePx),
            )

            // Trailing dots, streaming rightward from the handle center.
            val startX = animatedBounds.center.x
            val centerY = animatedBounds.center.y
            drawTrailDot(progressA, startX, centerY, dotTravelPx, dotRadiusPx, dotColor)
            drawTrailDot(progressB, startX, centerY, dotTravelPx, dotRadiusPx, dotColor)
            drawTrailDot(progressC, startX, centerY, dotTravelPx, dotRadiusPx, dotColor)
        }
    }
}

private fun DrawScope.drawTrailDot(
    progress: Float,
    startX: Float,
    centerY: Float,
    travel: Float,
    radius: Float,
    color: Color,
) {
    // Wrap progress into [0, 1)
    val p = progress - progress.toInt().toFloat()
    val x = startX + p * travel
    // Fade in quickly, fade out slowly as the dot streams rightward.
    val alpha = max(0f, 1f - p)
    drawCircle(
        color = color.copy(alpha = 0.9f * alpha),
        radius = radius,
        center = Offset(x, centerY),
    )
}
