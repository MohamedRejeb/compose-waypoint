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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
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
        // 1. Draw the scrim
        drawRect(color = style.overlayColor.copy(alpha = style.overlayAlpha))

        // 2. Punch hard cutouts for all targets
        for (bounds in allPaddedBounds) {
            drawCutout(bounds, style.shape, density)
        }

        // 3. Apply SoftEdge feather on top of the hard cutouts (still clearing)
        val effect = style.effect
        if (effect is SpotlightEffect.SoftEdge) {
            val fadeWidthPx = with(density) { effect.fadeWidth.toPx() }
            for (bounds in allPaddedBounds) {
                drawSoftEdge(bounds, fadeWidthPx)
            }
        }

        // 4. Draw on-top effects (Glow / Custom) after cutouts.
        when (effect) {
            is SpotlightEffect.None,
            is SpotlightEffect.SoftEdge -> Unit

            is SpotlightEffect.Glow -> {
                val radiusPx = with(density) { effect.radius.toPx() }
                for (bounds in allPaddedBounds) {
                    drawGlow(bounds, effect.color, effect.alpha, radiusPx)
                }
            }

            is SpotlightEffect.Custom -> {
                for (bounds in allPaddedBounds) {
                    effect.draw(this, bounds)
                }
            }
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

private fun DrawScope.drawCutout(
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

/**
 * Softens the boundary of an already-punched cutout by clearing a radial
 * gradient band that extends [fadeWidthPx] beyond the bounds.
 *
 * Approximation: uses a single radial-gradient clear centered on [bounds]
 * with an outer radius of `max(width, height) / 2 + fadeWidthPx`. Works
 * acceptably for all shape types since the hard cutout underneath keeps
 * the core fully transparent.
 */
private fun DrawScope.drawSoftEdge(bounds: Rect, fadeWidthPx: Float) {
    val innerRadius = max(bounds.width, bounds.height) / 2f
    val outerRadius = innerRadius + fadeWidthPx
    if (outerRadius <= 0f) return

    val innerStop = (innerRadius / outerRadius).coerceIn(0f, 1f)
    val brush = Brush.radialGradient(
        colorStops = arrayOf(
            0f to Color.Black,
            innerStop to Color.Black,
            1f to Color.Transparent,
        ),
        center = bounds.center,
        radius = outerRadius,
    )
    drawRect(
        brush = brush,
        topLeft = Offset(
            x = bounds.center.x - outerRadius,
            y = bounds.center.y - outerRadius,
        ),
        size = Size(outerRadius * 2f, outerRadius * 2f),
        blendMode = BlendMode.Clear,
    )
}

/**
 * Draws a radial halo centered on [bounds], peaking at [alpha] near the
 * cutout edge and fading linearly to 0 at `edge + radiusPx`.
 */
private fun DrawScope.drawGlow(
    bounds: Rect,
    color: Color,
    alpha: Float,
    radiusPx: Float,
) {
    val innerRadius = max(bounds.width, bounds.height) / 2f
    val outerRadius = innerRadius + radiusPx
    if (outerRadius <= 0f) return

    val innerStop = (innerRadius / outerRadius).coerceIn(0f, 1f)
    val brush = Brush.radialGradient(
        colorStops = arrayOf(
            0f to Color.Transparent,
            innerStop to color.copy(alpha = alpha),
            1f to Color.Transparent,
        ),
        center = bounds.center,
        radius = outerRadius,
    )
    drawRect(
        brush = brush,
        topLeft = Offset(
            x = bounds.center.x - outerRadius,
            y = bounds.center.y - outerRadius,
        ),
        size = Size(outerRadius * 2f, outerRadius * 2f),
        blendMode = BlendMode.SrcOver,
    )
}
