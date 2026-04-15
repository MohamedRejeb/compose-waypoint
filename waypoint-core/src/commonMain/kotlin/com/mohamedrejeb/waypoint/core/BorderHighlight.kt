package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity

/**
 * Renders a static colored shape around the target element.
 * No animation, no overlay. Supports both stroke and filled rendering.
 */
@Composable
internal fun BorderHighlight(
    targetBounds: Rect,
    style: HighlightStyle.Border,
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

    val drawStyle = if (style.filled) Fill else Stroke(width = borderWidthPx)

    Canvas(modifier = modifier) {
        drawShape(
            shape = style.shape,
            bounds = paddedBounds,
            color = style.color,
            drawStyle = drawStyle,
            density = density,
        )
    }
}
