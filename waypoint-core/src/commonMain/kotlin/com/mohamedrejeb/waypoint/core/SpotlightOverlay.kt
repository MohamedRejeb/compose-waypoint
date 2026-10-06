package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.max
import kotlin.math.pow

/**
 * Draws a semi-transparent overlay with transparent cutouts (spotlights)
 * around the target elements. Drawing only, touch blocking is a separate
 * layer ([SpotlightBlockers]) owned by the host.
 *
 * [targetBounds] returns the primary target first, then any additional
 * targets. It is empty for a step without a target, which draws the scrim
 * with no cutout. It is only read while drawing, so animating bounds do not
 * recompose anything.
 */
@Composable
internal fun SpotlightOverlay(
    targetBounds: () -> List<Rect>,
    style: HighlightStyle.Spotlight,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val paddedBounds: () -> List<Rect> = {
        targetBounds().map { padBounds(it, style.padding, density, layoutDirection) }
    }

    Canvas(
        modifier = modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
    ) {
        val allPaddedBounds = paddedBounds()

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
                drawSoftEdge(bounds, style.shape, fadeWidthPx, density)
            }
        }

        // 4. Draw on-top effects (Glow / Custom) after cutouts.
        when (effect) {
            is SpotlightEffect.None,
            is SpotlightEffect.SoftEdge -> Unit

            is SpotlightEffect.Glow -> {
                val radiusPx = with(density) { effect.radius.toPx() }
                for (bounds in allPaddedBounds) {
                    drawGlow(bounds, style.shape, effect.color, effect.alpha, radiusPx, density)
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

/**
 * The areas that stay interactive for [targetBounds] (primary target first,
 * then additional targets) under this style, in the host's coordinates: the
 * padded, shape-aware cutouts for styles that draw a shape around the target
 * (see [cutoutBounds]), the plain target bounds for the others.
 */
internal fun HighlightStyle.interactiveBounds(
    targetBounds: List<Rect>,
    density: Density,
    layoutDirection: LayoutDirection,
): List<Rect> = when (this) {
    is HighlightStyle.Spotlight ->
        targetBounds.map { cutoutBounds(padBounds(it, padding, density, layoutDirection), shape) }

    is HighlightStyle.Pulse ->
        targetBounds.map { cutoutBounds(padBounds(it, padding, density, layoutDirection), shape) }

    is HighlightStyle.Border ->
        targetBounds.map { cutoutBounds(padBounds(it, padding, density, layoutDirection), shape) }

    is HighlightStyle.Ripple,
    is HighlightStyle.None,
    is HighlightStyle.Custom -> targetBounds
}

/**
 * The rectangle that pointer input treats as the cutout drawn for
 * [paddedBounds]. Every shape stays within its bounds except the circle, which
 * is drawn around the center with a radius of half the longer side and so
 * reaches beyond a non-square target: for it this is the circle's bounding
 * square.
 */
internal fun cutoutBounds(paddedBounds: Rect, shape: SpotlightShape): Rect = when (shape) {
    is SpotlightShape.Circle -> Rect(
        center = paddedBounds.center,
        radius = max(paddedBounds.width, paddedBounds.height) / 2f,
    )

    is SpotlightShape.Rect,
    is SpotlightShape.RoundedRect,
    is SpotlightShape.Pill -> paddedBounds
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
 * Softens the boundary of an already-punched cutout with a feather that
 * follows the cutout's [shape]. Implemented by iteratively drawing filled
 * expanded copies of the shape with [BlendMode.DstOut]: pixels close to the
 * cutout edge get hit by more iterations (cleared more), while pixels at
 * the outer edge of the fade band are hit once (cleared less). This yields
 * a shape-aware gradient, unlike a single radial clear which would render
 * a circle regardless of the underlying shape.
 */
private fun DrawScope.drawSoftEdge(
    bounds: Rect,
    shape: SpotlightShape,
    fadeWidthPx: Float,
    density: Density,
) {
    if (fadeWidthPx <= 0f) return

    // For an exponential fade, alphaPerStep = 1 - 0.5^(2/steps) means a pixel
    // at the midpoint of the fade band (hit by steps/2 iterations) has
    // remaining destination alpha of 0.5 - giving a visually linear fade.
    val steps = 24
    val alphaPerStep = 1f - 0.5f.pow(2f / steps)

    for (i in 1..steps) {
        val t = i.toFloat() / steps
        val expansion = t * fadeWidthPx
        drawExpandedCutoutDstOut(bounds, shape, density, expansion, alphaPerStep)
    }
}

private fun DrawScope.drawExpandedCutoutDstOut(
    bounds: Rect,
    shape: SpotlightShape,
    density: Density,
    expansion: Float,
    alpha: Float,
) {
    val expanded = Rect(
        left = bounds.left - expansion,
        top = bounds.top - expansion,
        right = bounds.right + expansion,
        bottom = bounds.bottom + expansion,
    )
    val color = Color.Black.copy(alpha = alpha)
    when (shape) {
        is SpotlightShape.Circle -> {
            val radius = max(expanded.width, expanded.height) / 2f
            drawCircle(
                color = color,
                center = expanded.center,
                radius = radius,
                blendMode = BlendMode.DstOut,
            )
        }

        is SpotlightShape.Rect -> {
            drawRect(
                color = color,
                topLeft = expanded.topLeft,
                size = expanded.size,
                blendMode = BlendMode.DstOut,
            )
        }

        is SpotlightShape.RoundedRect -> {
            val baseCornerPx = with(density) { shape.cornerRadius.toPx() }
            val cornerRadiusPx = baseCornerPx + expansion
            drawRoundRect(
                color = color,
                topLeft = expanded.topLeft,
                size = expanded.size,
                cornerRadius = CornerRadius(cornerRadiusPx),
                blendMode = BlendMode.DstOut,
            )
        }

        is SpotlightShape.Pill -> {
            val cornerRadiusPx = expanded.height / 2f
            drawRoundRect(
                color = color,
                topLeft = expanded.topLeft,
                size = expanded.size,
                cornerRadius = CornerRadius(cornerRadiusPx),
                blendMode = BlendMode.DstOut,
            )
        }
    }
}

/**
 * Draws a halo around the cutout at [bounds] that follows the cutout's
 * [shape]: strongest at the cutout's edge, fading to nothing at
 * `edge + radiusPx`, and never drawn inside the cutout.
 *
 * It is built from concentric outlines of the shape, each one a little larger
 * and a little fainter than the last. A single radial gradient would be a
 * circle whatever the shape, and would tint a wide target itself.
 */
private fun DrawScope.drawGlow(
    bounds: Rect,
    shape: SpotlightShape,
    color: Color,
    alpha: Float,
    radiusPx: Float,
    density: Density,
) {
    if (radiusPx <= 0f || alpha <= 0f) return

    val bandWidth = radiusPx / GlowSteps
    // Bands overlap slightly so no gap shows between them.
    val stroke = Stroke(width = bandWidth + GlowBandOverlapPx)
    for (i in 0 until GlowSteps) {
        val bandCenter = (i + 0.5f) * bandWidth
        val fade = 1f - bandCenter / radiusPx
        drawCutoutOutline(
            bounds = bounds,
            shape = shape,
            density = density,
            // Keeps the first band's inner side on the cutout's edge.
            expansion = bandCenter + GlowBandOverlapPx / 2f,
            color = color.copy(alpha = alpha * fade * fade),
            stroke = stroke,
        )
    }
}

private const val GlowSteps = 24
private const val GlowBandOverlapPx = 0.5f

/** Strokes the outline of the cutout at [bounds], grown outwards by [expansion]. */
private fun DrawScope.drawCutoutOutline(
    bounds: Rect,
    shape: SpotlightShape,
    density: Density,
    expansion: Float,
    color: Color,
    stroke: Stroke,
) {
    val expanded = bounds.inflate(expansion)
    when (shape) {
        is SpotlightShape.Circle -> drawCircle(
            color = color,
            center = bounds.center,
            radius = max(bounds.width, bounds.height) / 2f + expansion,
            style = stroke,
        )

        is SpotlightShape.Rect -> drawRoundRect(
            color = color,
            topLeft = expanded.topLeft,
            size = expanded.size,
            // Rounds the halo around a square corner, as light spreads.
            cornerRadius = CornerRadius(expansion),
            style = stroke,
        )

        is SpotlightShape.RoundedRect -> drawRoundRect(
            color = color,
            topLeft = expanded.topLeft,
            size = expanded.size,
            cornerRadius = CornerRadius(with(density) { shape.cornerRadius.toPx() } + expansion),
            style = stroke,
        )

        is SpotlightShape.Pill -> drawRoundRect(
            color = color,
            topLeft = expanded.topLeft,
            size = expanded.size,
            cornerRadius = CornerRadius(expanded.height / 2f),
            style = stroke,
        )
    }
}
