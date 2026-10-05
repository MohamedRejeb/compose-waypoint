package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.roundToIntRect

/**
 * The touch-blocking half of the spotlight: invisible boxes that swallow
 * pointer input so the content underneath cannot be used during a step.
 *
 * Without [passThrough] a single blocker covers everything and a tap is
 * reported as [onTargetClick] or [onOverlayClick] depending on whether it
 * lands in one of the [holes]. With [passThrough] the blockers only cover the
 * area outside the holes (see [blockerRects]), so every gesture that starts
 * inside a hole reaches the content below, and taps outside are reported as
 * [onOverlayClick].
 *
 * [holes] is only read in the layout phase and on taps. A fixed number of
 * blockers is composed (see [maxBlockerCount]) and they are sized and placed
 * during layout, so a bounds animation neither recomposes anything nor
 * restarts their pointer input (which would drop taps that land mid-animation).
 */
@Composable
internal fun SpotlightBlockers(
    holes: () -> List<Rect>,
    passThrough: Boolean,
    onOverlayClick: () -> Unit,
    onTargetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentHoles by rememberUpdatedState(holes)
    val currentOnOverlayClick by rememberUpdatedState(onOverlayClick)
    val currentOnTargetClick by rememberUpdatedState(onTargetClick)

    // Only the number of holes is needed in composition, and it does not
    // change while their bounds animate.
    val holeCount by remember { derivedStateOf { currentHoles().size } }
    val blockerCount = if (passThrough) maxBlockerCount(holeCount) else 1

    // Where the last layout pass put each blocker, to turn a tap position in a
    // blocker into a position in the overlay.
    val placedRects = remember { PlacedRects() }

    Layout(
        content = {
            repeat(blockerCount) { index ->
                key(index) {
                    Blocker(
                        onTap = { offset ->
                            val origin = placedRects.value.getOrNull(index)?.topLeft ?: Offset.Zero
                            val position = origin + offset
                            if (currentHoles().any { it.contains(position) }) {
                                currentOnTargetClick()
                            } else {
                                currentOnOverlayClick()
                            }
                        },
                    )
                }
            }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val area = Size(width.toFloat(), height.toFloat())
        val rects = if (passThrough) blockerRects(area, currentHoles()) else listOf(area.toRect())
        placedRects.value = rects

        // Blockers beyond the rectangles needed right now collapse to nothing.
        val placed = measurables.mapIndexed { index, measurable ->
            val bounds = rects.getOrNull(index)?.roundToIntRect() ?: IntRect.Zero
            val placeable = measurable.measure(
                Constraints.fixed(bounds.width.coerceAtLeast(0), bounds.height.coerceAtLeast(0)),
            )
            placeable to bounds.topLeft
        }
        layout(width, height) {
            placed.forEach { (placeable, position) -> placeable.place(position) }
        }
    }
}

/** Plain holder written during layout and read on taps, deliberately not snapshot state. */
private class PlacedRects {
    var value: List<Rect> = emptyList()
}

/**
 * An invisible box that keeps every pointer event that lands on it away from
 * the content below and from ancestors (scroll containers, clickable parents),
 * and reports taps through [onTap] with the tap position in its own space.
 */
@Composable
private fun Blocker(onTap: (Offset) -> Unit) {
    val currentOnTap by rememberUpdatedState(onTap)
    Box(
        modifier = Modifier
            // Outer modifier, so it sees events in the main pass after the tap
            // detector below and consumes whatever that one left (drags, mouse
            // wheel), keeping ancestors from reacting to them.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Main).changes.forEach { it.consume() }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset -> currentOnTap(offset) }
            },
    )
}
