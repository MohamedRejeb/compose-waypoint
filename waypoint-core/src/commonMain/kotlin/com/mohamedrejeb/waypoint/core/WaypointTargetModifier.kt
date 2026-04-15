package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Marks this composable as a Waypoint tour target.
 *
 * The composable's position and size will be tracked and used to render the
 * highlight and position the tooltip when this target's step is active.
 * A [BringIntoViewRequester] is automatically attached so the tour can
 * scroll this target into view before showing the step.
 *
 * Coordinates are computed relative to the [WaypointHost] Box (not the window
 * root), so the spotlight renders correctly even inside Dialogs and Sheets.
 *
 * @param state the [WaypointState] managing the tour
 * @param key the target key identifying this composable in the step list
 */
public fun <K> Modifier.waypointTarget(
    state: WaypointState<K>,
    key: K,
): Modifier = composed {
    val currentKey = remember(key) { key }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    DisposableEffect(currentKey) {
        state.registerBringIntoViewRequester(currentKey, bringIntoViewRequester)
        onDispose {
            state.unregisterTarget(currentKey)
        }
    }

    this
        .bringIntoViewRequester(bringIntoViewRequester)
        .onGloballyPositioned { coordinates ->
            if (!coordinates.isAttached) return@onGloballyPositioned

            val hostCoords = state.hostCoordinates
            if (hostCoords != null && hostCoords.isAttached) {
                val bounds = hostCoords.localBoundingBoxOf(coordinates)

                // Detect if the target is scrolled out of the host's visible area.
                // Compare the target's boundsInRoot with the host's boundsInRoot.
                // If they don't overlap, the target is clipped by a scroll container.
                val targetInRoot = coordinates.boundsInRoot()
                val hostInRoot = hostCoords.boundsInRoot()
                val isVisible = targetInRoot.overlaps(hostInRoot) &&
                    targetInRoot.width > 1f && targetInRoot.height > 1f

                if (isVisible && bounds != Rect.Zero) {
                    state.registerTarget(currentKey, bounds)
                } else {
                    state.targetCoordinates.remove(currentKey)
                }
            } else {
                val bounds = coordinates.boundsInRoot()
                if (bounds != Rect.Zero) {
                    state.registerTarget(currentKey, bounds)
                }
            }
        }
}
