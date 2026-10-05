package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.unit.toSize

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
 * Blockers are keyed by index and moved in the layout phase, so their pointer
 * input survives the frames of a bounds animation instead of restarting (which
 * would drop taps that land mid-animation).
 */
@Composable
internal fun SpotlightBlockers(
    holes: List<Rect>,
    passThrough: Boolean,
    onOverlayClick: () -> Unit,
    onTargetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    // Null means one blocker covering everything. That is also the safe
    // fallback for the first frame of a pass-through step, before the size
    // needed to decompose the area is known.
    val rects = if (passThrough && size != IntSize.Zero) {
        blockerRects(size.toSize(), holes)
    } else {
        null
    }

    val currentHoles by rememberUpdatedState(holes)
    val currentRects by rememberUpdatedState(rects)
    val currentOnOverlayClick by rememberUpdatedState(onOverlayClick)
    val currentOnTargetClick by rememberUpdatedState(onTargetClick)

    Layout(
        content = {
            repeat(rects?.size ?: 1) { index ->
                key(index) {
                    Blocker(
                        onTap = { offset ->
                            val origin = currentRects?.getOrNull(index)?.topLeft ?: Offset.Zero
                            val position = origin + offset
                            if (currentHoles.any { it.contains(position) }) {
                                currentOnTargetClick()
                            } else {
                                currentOnOverlayClick()
                            }
                        },
                    )
                }
            }
        },
        modifier = modifier.onSizeChanged { size = it },
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val placed = measurables.mapIndexed { index, measurable ->
            val bounds = rects?.getOrNull(index)?.roundToIntRect() ?: IntRect(0, 0, width, height)
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
