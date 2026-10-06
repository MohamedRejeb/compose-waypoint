package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/**
 * Custom [PopupPositionProvider] that positions the tooltip near the spotlight target.
 *
 * Handles auto-placement, flip logic, and screen edge clamping.
 *
 * [targetBounds] is expressed relative to the popup's anchor (the layout node
 * where the popup is composed). It is translated by the anchor's window
 * position inside [calculatePosition], using the `anchorBounds` the popup
 * machinery itself computes. Deriving the window offset this way keeps the
 * math consistent with the frame the popup is positioned in, which matters on
 * Android where Dialog/BottomSheet windows have their own frames and
 * `positionInWindow()` is not guaranteed to match them.
 *
 * The tooltip is clamped to the screen margin on both axes, so one that is
 * larger than the space on its side slides over the target instead of
 * running off screen.
 */
internal class WaypointPositionProvider(
    private val targetBounds: Rect,
    private val requestedPlacement: TooltipPlacement,
    private val spacingPx: Float,
    private val screenMarginPx: Float,
    initialPlacement: ResolvedPlacement = ResolvedPlacement.Bottom,
    private val arrowEdgeInsetPx: Float = DEFAULT_ARROW_EDGE_INSET,
) : PopupPositionProvider {

    /** The resolved placement after layout, used to orient the arrow */
    var resolvedPlacement: ResolvedPlacement by mutableStateOf(initialPlacement)
        private set

    /** Horizontal offset of the arrow center relative to the tooltip's left edge */
    var arrowHorizontalOffset: Float by mutableStateOf(0f)
        private set

    /** Vertical offset of the arrow center relative to the tooltip's top edge */
    var arrowVerticalOffset: Float by mutableStateOf(0f)
        private set

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val tooltipWidth = popupContentSize.width.toFloat()
        val tooltipHeight = popupContentSize.height.toFloat()

        // Anchor-relative -> popup-window coordinates.
        val targetBounds = targetBounds.translate(
            anchorBounds.left.toFloat(),
            anchorBounds.top.toFloat(),
        )

        val spaceTop = targetBounds.top
        val spaceBottom = windowSize.height - targetBounds.bottom
        val spaceStart = if (layoutDirection == LayoutDirection.Ltr) {
            targetBounds.left
        } else {
            windowSize.width - targetBounds.right
        }
        val spaceEnd = if (layoutDirection == LayoutDirection.Ltr) {
            windowSize.width - targetBounds.right
        } else {
            targetBounds.left
        }

        val placement = resolvePlacement(
            requested = requestedPlacement,
            spaceTop = spaceTop,
            spaceBottom = spaceBottom,
            spaceStart = spaceStart,
            spaceEnd = spaceEnd,
            tooltipWidth = tooltipWidth,
            tooltipHeight = tooltipHeight,
            layoutDirection = layoutDirection,
        )

        resolvedPlacement = placement

        val (x, y) = calculateOffset(
            placement = placement,
            targetBounds = targetBounds,
            tooltipWidth = tooltipWidth,
            tooltipHeight = tooltipHeight,
            windowWidth = windowSize.width.toFloat(),
            windowHeight = windowSize.height.toFloat(),
            layoutDirection = layoutDirection,
        )

        // Calculate arrow offset
        when (placement) {
            ResolvedPlacement.Top, ResolvedPlacement.Bottom -> {
                val minOffset = arrowEdgeInsetPx
                val maxOffset = tooltipWidth - arrowEdgeInsetPx
                arrowHorizontalOffset = if (maxOffset >= minOffset) {
                    (targetBounds.center.x - x).coerceIn(minOffset, maxOffset)
                } else {
                    tooltipWidth / 2f
                }
            }

            ResolvedPlacement.Start, ResolvedPlacement.End -> {
                val minOffset = arrowEdgeInsetPx
                val maxOffset = tooltipHeight - arrowEdgeInsetPx
                arrowVerticalOffset = if (maxOffset >= minOffset) {
                    (targetBounds.center.y - y).coerceIn(minOffset, maxOffset)
                } else {
                    tooltipHeight / 2f
                }
            }
        }

        return IntOffset(x.toInt(), y.toInt())
    }

    private fun resolvePlacement(
        requested: TooltipPlacement,
        spaceTop: Float,
        spaceBottom: Float,
        spaceStart: Float,
        spaceEnd: Float,
        tooltipWidth: Float,
        tooltipHeight: Float,
        layoutDirection: LayoutDirection,
    ): ResolvedPlacement {
        // Convert Start/End to absolute directions
        fun toResolved(placement: TooltipPlacement): ResolvedPlacement = when (placement) {
            TooltipPlacement.Top -> ResolvedPlacement.Top
            TooltipPlacement.Bottom -> ResolvedPlacement.Bottom
            TooltipPlacement.Start -> ResolvedPlacement.Start
            TooltipPlacement.End -> ResolvedPlacement.End
            TooltipPlacement.Auto -> ResolvedPlacement.Bottom // fallback
        }

        fun fitsVertical(space: Float) = space >= tooltipHeight + spacingPx
        fun fitsHorizontal(space: Float) = space >= tooltipWidth + spacingPx

        if (requested != TooltipPlacement.Auto) {
            val resolved = toResolved(requested)
            val fits = when (resolved) {
                ResolvedPlacement.Top -> fitsVertical(spaceTop)
                ResolvedPlacement.Bottom -> fitsVertical(spaceBottom)
                ResolvedPlacement.Start -> fitsHorizontal(spaceStart)
                ResolvedPlacement.End -> fitsHorizontal(spaceEnd)
            }
            if (fits) return resolved

            // Try the opposite side
            val flipped = when (resolved) {
                ResolvedPlacement.Top -> ResolvedPlacement.Bottom
                ResolvedPlacement.Bottom -> ResolvedPlacement.Top
                ResolvedPlacement.Start -> ResolvedPlacement.End
                ResolvedPlacement.End -> ResolvedPlacement.Start
            }
            val flippedFits = when (flipped) {
                ResolvedPlacement.Top -> fitsVertical(spaceTop)
                ResolvedPlacement.Bottom -> fitsVertical(spaceBottom)
                ResolvedPlacement.Start -> fitsHorizontal(spaceStart)
                ResolvedPlacement.End -> fitsHorizontal(spaceEnd)
            }
            if (flippedFits) return flipped

            // Fall through to auto
        }

        // Auto: pick the side with the most space
        data class Candidate(val placement: ResolvedPlacement, val space: Float)

        val candidates = listOf(
            Candidate(ResolvedPlacement.Bottom, spaceBottom),
            Candidate(ResolvedPlacement.Top, spaceTop),
            Candidate(ResolvedPlacement.End, spaceEnd),
            Candidate(ResolvedPlacement.Start, spaceStart),
        )

        return candidates.maxBy { it.space }.placement
    }

    private fun calculateOffset(
        placement: ResolvedPlacement,
        targetBounds: Rect,
        tooltipWidth: Float,
        tooltipHeight: Float,
        windowWidth: Float,
        windowHeight: Float,
        layoutDirection: LayoutDirection,
    ): Pair<Float, Float> {
        val margin = screenMarginPx

        // The cross axis is clamped to the margins so the tooltip stays on
        // screen; the placement axis is clamped too, so a tooltip larger than
        // the space on its side slides over the target rather than off screen.
        fun clampX(x: Float) = x.coerceInOrCenter(margin, windowWidth - tooltipWidth - margin)
        fun clampY(y: Float) = y.coerceInOrCenter(margin, windowHeight - tooltipHeight - margin)

        return when (placement) {
            ResolvedPlacement.Bottom -> {
                val x = clampX(targetBounds.center.x - tooltipWidth / 2f)
                val y = clampY(targetBounds.bottom + spacingPx)
                x to y
            }

            ResolvedPlacement.Top -> {
                val x = clampX(targetBounds.center.x - tooltipWidth / 2f)
                val y = clampY(targetBounds.top - tooltipHeight - spacingPx)
                x to y
            }

            ResolvedPlacement.End -> {
                val x = clampX(
                    if (layoutDirection == LayoutDirection.Ltr) {
                        targetBounds.right + spacingPx
                    } else {
                        targetBounds.left - tooltipWidth - spacingPx
                    },
                )
                val y = clampY(targetBounds.center.y - tooltipHeight / 2f)
                x to y
            }

            ResolvedPlacement.Start -> {
                val x = clampX(
                    if (layoutDirection == LayoutDirection.Ltr) {
                        targetBounds.left - tooltipWidth - spacingPx
                    } else {
                        targetBounds.right + spacingPx
                    },
                )
                val y = clampY(targetBounds.center.y - tooltipHeight / 2f)
                x to y
            }
        }
    }

    internal companion object {
        /**
         * Minimum distance in px between the arrow center and the tooltip's
         * corners, so the arrow doesn't land on a rounded corner. Callers with
         * density access should pass a dp-scaled value instead.
         */
        const val DEFAULT_ARROW_EDGE_INSET = 24f

        /**
         * Like coerceIn, but when the tooltip is too large to satisfy both
         * margins (max < min, e.g. a tiny window), centers the overflow
         * instead of throwing.
         */
        fun Float.coerceInOrCenter(min: Float, max: Float): Float =
            if (max < min) (min + max) / 2f else coerceIn(min, max)
    }
}
