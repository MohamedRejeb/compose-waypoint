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
 * Bounds are recorded in the coordinate space of the nearest [WaypointHost]
 * (or [WaypointOverlayHost]) in the composition. For targets that live in a
 * different composition tree (e.g. inside a Dialog or Sheet), place a
 * [WaypointOverlayHost] that shares the same [WaypointState] inside that tree
 * so its targets register against the correct host.
 *
 * @param state the [WaypointState] managing the tour
 * @param key the target key identifying this composable in the step list
 */
public fun <K> Modifier.waypointTarget(
    state: WaypointState<K>,
    key: K,
): Modifier = composed {
    val currentKey = remember(key) { key }
    val hostId = LocalWaypointHostId.current
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
            // No host in scope — target can't be registered anywhere useful.
            if (hostId == null) return@onGloballyPositioned

            val hostCoords = state.hostCoordinatesMap[hostId] ?: return@onGloballyPositioned
            if (!hostCoords.isAttached) return@onGloballyPositioned

            val bounds = try {
                hostCoords.localBoundingBoxOf(coordinates)
            } catch (_: IllegalArgumentException) {
                // Target is in a different hierarchy than this host. Shouldn't
                // happen with correct CompositionLocal wiring, but guard anyway.
                return@onGloballyPositioned
            }

            // Scroll-out-of-view detection: use root-space bounds intersection
            // so we can tell when a target has been scrolled off the host.
            val targetInRoot = coordinates.boundsInRoot()
            val hostInRoot = hostCoords.boundsInRoot()
            val isVisible = targetInRoot.overlaps(hostInRoot) &&
                bounds.width > 1f && bounds.height > 1f

            if (isVisible && bounds != Rect.Zero) {
                state.registerTarget(currentKey, hostId, bounds)
            } else {
                // Scrolled out of view (or not yet laid out): clear bounds but
                // keep the host association so the tour still knows which host
                // owns this step until the composable is actually disposed.
                state.clearTargetBounds(currentKey)
            }
        }
}
