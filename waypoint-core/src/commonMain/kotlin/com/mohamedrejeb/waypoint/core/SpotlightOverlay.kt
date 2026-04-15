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
import kotlin.math.max

/**
 * Renders a semi-transparent overlay with a transparent cutout (spotlight)
 * around the target element.
 *
 * When [allowTargetInteraction] is true, the overlay becomes visual-only
 * with no touch interception, so all taps pass through to content underneath.
 */
@Composable
internal fun SpotlightOverlay(
    targetBounds: Rect,
    style: HighlightStyle.Spotlight,
    allowTargetInteraction: Boolean,
    onOverlayClick: () -> Unit,
    onTargetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    val paddedBounds = with(density) {
        Rect(
            left = targetBounds.left - style.padding.start.toPx(),
            top = targetBounds.top - style.padding.top.toPx(),
            right = targetBounds.right + style.padding.end.toPx(),
            bottom = targetBounds.bottom + style.padding.bottom.toPx(),
        )
    }

    // When allowTargetInteraction is true, no touch handling at all --
    // the overlay is purely visual so taps pass through to content below.
    val touchModifier = if (!allowTargetInteraction) {
        Modifier.pointerInput(targetBounds) {
            detectTapGestures { offset ->
                if (paddedBounds.contains(offset)) {
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

        // Draw the cutout
        when (style.shape) {
            is SpotlightShape.Circle -> {
                val radius = max(paddedBounds.width, paddedBounds.height) / 2f
                drawCircle(
                    color = Color.Black,
                    center = paddedBounds.center,
                    radius = radius,
                    blendMode = BlendMode.Clear,
                )
            }

            is SpotlightShape.Rect -> {
                drawRect(
                    color = Color.Black,
                    topLeft = paddedBounds.topLeft,
                    size = paddedBounds.size,
                    blendMode = BlendMode.Clear,
                )
            }

            is SpotlightShape.RoundedRect -> {
                val cornerRadiusPx = with(density) { style.shape.cornerRadius.toPx() }
                drawRoundRect(
                    color = Color.Black,
                    topLeft = paddedBounds.topLeft,
                    size = paddedBounds.size,
                    cornerRadius = CornerRadius(cornerRadiusPx),
                    blendMode = BlendMode.Clear,
                )
            }

            is SpotlightShape.Pill -> {
                val cornerRadiusPx = paddedBounds.height / 2f
                drawRoundRect(
                    color = Color.Black,
                    topLeft = paddedBounds.topLeft,
                    size = paddedBounds.size,
                    cornerRadius = CornerRadius(cornerRadiusPx),
                    blendMode = BlendMode.Clear,
                )
            }
        }
    }
}
