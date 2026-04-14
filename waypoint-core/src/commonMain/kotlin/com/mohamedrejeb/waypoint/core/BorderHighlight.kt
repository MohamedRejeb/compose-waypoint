package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity

/**
 * Renders a static colored border around the target element.
 * No animation, no overlay.
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

    Canvas(modifier = modifier) {
        drawShapeBorder(
            shape = style.shape,
            bounds = paddedBounds,
            color = style.color,
            strokeWidth = borderWidthPx,
            density = density,
        )
    }
}
