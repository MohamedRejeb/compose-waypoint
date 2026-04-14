package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

/**
 * Renders a semi-transparent overlay with a transparent cutout (spotlight)
 * around the target element.
 */
@Composable
internal fun SpotlightOverlay(
    targetBounds: Rect,
    spotlightShape: SpotlightShape,
    spotlightPadding: SpotlightPadding,
    overlayColor: Color,
    overlayAlpha: Float,
    onOverlayClick: () -> Unit,
    onTargetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    val paddedBounds = with(density) {
        Rect(
            left = targetBounds.left - spotlightPadding.start.toPx(),
            top = targetBounds.top - spotlightPadding.top.toPx(),
            right = targetBounds.right + spotlightPadding.end.toPx(),
            bottom = targetBounds.bottom + spotlightPadding.bottom.toPx(),
        )
    }

    Canvas(
        modifier = modifier
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .pointerInput(targetBounds) {
                detectTapGestures { offset ->
                    if (paddedBounds.contains(offset)) {
                        onTargetClick()
                    } else {
                        onOverlayClick()
                    }
                }
            },
    ) {
        // Draw the scrim
        drawRect(color = overlayColor.copy(alpha = overlayAlpha))

        // Draw the cutout
        when (spotlightShape) {
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
                val cornerRadiusPx = with(density) { spotlightShape.cornerRadius.toPx() }
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
