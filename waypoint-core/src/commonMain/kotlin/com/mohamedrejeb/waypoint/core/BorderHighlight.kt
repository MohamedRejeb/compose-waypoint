package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity

/**
 * Renders a static colored shape around the target elements.
 * No animation, no overlay. Supports both stroke and filled rendering,
 * and multiple targets.
 */
@Composable
internal fun BorderHighlight(
    targetBounds: Rect,
    additionalBounds: List<Rect>,
    style: HighlightStyle.Border,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val borderWidthPx = with(density) { style.borderWidth.toPx() }

    val allBounds = buildList {
        add(padBounds(targetBounds, style.padding, density))
        additionalBounds.forEach { add(padBounds(it, style.padding, density)) }
    }

    val drawStyle = if (style.filled) Fill else Stroke(width = borderWidthPx)

    Canvas(modifier = modifier) {
        for (bounds in allBounds) {
            drawShape(
                shape = style.shape,
                bounds = bounds,
                color = style.color,
                drawStyle = drawStyle,
                density = density,
            )
        }
    }
}

private fun padBounds(bounds: Rect, padding: SpotlightPadding, density: androidx.compose.ui.unit.Density): Rect =
    with(density) {
        Rect(
            left = bounds.left - padding.start.toPx(),
            top = bounds.top - padding.top.toPx(),
            right = bounds.right + padding.end.toPx(),
            bottom = bounds.bottom + padding.bottom.toPx(),
        )
    }
