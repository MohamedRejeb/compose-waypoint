package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.max

/**
 * Renders a semi-transparent overlay with transparent cutouts (spotlights)
 * around the target elements.
 *
 * Supports multiple cutouts for multi-element highlight. The primary target
 * cutout is always drawn; additional cutouts are drawn for secondary targets.
 *
 * When [allowTargetInteraction] is true, the overlay becomes visual-only
 * with no touch interception, so all taps pass through to content underneath.
 */
@Composable
internal fun SpotlightOverlay(
    targetBounds: Rect,
    additionalBounds: List<Rect>,
    style: HighlightStyle.Spotlight,
    allowTargetInteraction: Boolean,
    onOverlayClick: () -> Unit,
    onTargetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val paddedBounds = padBounds(targetBounds, style.padding, density, layoutDirection)
    val allPaddedBounds = buildList {
        add(paddedBounds)
        additionalBounds.forEach { add(padBounds(it, style.padding, density, layoutDirection)) }
    }

    val touchModifier = if (!allowTargetInteraction) {
        Modifier.pointerInput(targetBounds, additionalBounds) {
            detectTapGestures { offset ->
                val tappedInCutout = allPaddedBounds.any { it.contains(offset) }
                if (tappedInCutout) {
                    onTargetClick()
                } else {
                    onOverlayClick()
                }
            }
        }
    } else {
        Modifier
    }

    Canvas(
        modifier = modifier
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .then(touchModifier),
    ) {
        // Draw the scrim
        drawRect(color = style.overlayColor.copy(alpha = style.overlayAlpha))

        // Draw cutouts for all targets
        for (bounds in allPaddedBounds) {
            drawCutout(bounds, style.shape, density)
        }
    }
}

private fun padBounds(
    bounds: Rect,
    padding: SpotlightPadding,
    density: Density,
    layoutDirection: LayoutDirection,
): Rect = with(density) {
    val startPx = padding.start.toPx()
    val endPx = padding.end.toPx()
    val leftPad = if (layoutDirection == LayoutDirection.Ltr) startPx else endPx
    val rightPad = if (layoutDirection == LayoutDirection.Ltr) endPx else startPx
    Rect(
        left = bounds.left - leftPad,
        top = bounds.top - padding.top.toPx(),
        right = bounds.right + rightPad,
        bottom = bounds.bottom + padding.bottom.toPx(),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCutout(
    bounds: Rect,
    shape: SpotlightShape,
    density: Density,
) {
    when (shape) {
        is SpotlightShape.Circle -> {
            val radius = max(bounds.width, bounds.height) / 2f
            drawCircle(
                color = Color.Black,
                center = bounds.center,
                radius = radius,
                blendMode = BlendMode.Clear,
            )
        }

        is SpotlightShape.Rect -> {
            drawRect(
                color = Color.Black,
                topLeft = bounds.topLeft,
                size = bounds.size,
                blendMode = BlendMode.Clear,
            )
        }

        is SpotlightShape.RoundedRect -> {
            val cornerRadiusPx = with(density) { shape.cornerRadius.toPx() }
            drawRoundRect(
                color = Color.Black,
                topLeft = bounds.topLeft,
                size = bounds.size,
                cornerRadius = CornerRadius(cornerRadiusPx),
                blendMode = BlendMode.Clear,
            )
        }

        is SpotlightShape.Pill -> {
            val cornerRadiusPx = bounds.height / 2f
            drawRoundRect(
                color = Color.Black,
                topLeft = bounds.topLeft,
                size = bounds.size,
                cornerRadius = CornerRadius(cornerRadiusPx),
                blendMode = BlendMode.Clear,
            )
        }
    }
}
