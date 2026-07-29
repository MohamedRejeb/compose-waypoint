package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * Experimental modifier for targets that cannot be wrapped by a composable,
 * for example shapes drawn inside a Canvas or regions of an editor viewport.
 *
 * Apply to the composable that owns the drawing (typically the Canvas itself).
 * The [boundsInCanvas] lambda returns the target's bounds in the local
 * coordinate space of this composable. The modifier tracks this composable's
 * position under the nearest [WaypointHost] and re-registers the target
 * whenever [boundsInCanvas] or the composable's position changes.
 *
 * Any Compose state read inside [boundsInCanvas] will drive re-registration,
 * so animated offsets, pan/zoom transforms, and drag state all work.
 *
 * For full manual control (for example to register a target from inside a
 * [androidx.compose.runtime.SideEffect] where no modifier can be applied),
 * use [WaypointState.setTargetBounds] directly.
 *
 * @param state the [WaypointState] managing the tour
 * @param key the target key identifying this target in the step list
 * @param boundsInCanvas lambda returning the target's [Rect] in this
 *   composable's local coordinate space, evaluated reactively
 */
@ExperimentalWaypointApi
public fun <K> Modifier.waypointCanvasTarget(
    state: WaypointState<K>,
    key: K,
    boundsInCanvas: () -> Rect,
): Modifier = composed {
    val currentKey = remember(key) { key }
    val hostId = LocalWaypointHostId.current
    val currentBoundsInCanvas by rememberUpdatedState(boundsInCanvas)
    var canvasCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    // Tick bumped on every onGloballyPositioned callback. onGloballyPositioned
    // often re-delivers the same LayoutCoordinates instance across re-layouts
    // (e.g. when the host scrolls), so mutableStateOf dedupes the write by
    // referential equality and snapshotFlow never wakes. Reading this tick
    // inside snapshotFlow forces re-evaluation on every layout pass.
    var tick by remember { mutableIntStateOf(0) }

    DisposableEffect(currentKey) {
        onDispose {
            state.unregisterTarget(currentKey)
        }
    }

    if (hostId != null) {
        LaunchedEffect(currentKey, hostId) {
            snapshotFlow {
                @Suppress("UNUSED_EXPRESSION")
                tick // subscribe to re-layout signal
                val coords = canvasCoords ?: return@snapshotFlow null
                if (!coords.isAttached) return@snapshotFlow null
                val hostCoords = state.hostCoordinatesMap[hostId] ?: return@snapshotFlow null
                if (!hostCoords.isAttached) return@snapshotFlow null

                val canvasRect = currentBoundsInCanvas()
                // Map both corners through the coordinate systems so uniform
                // scale (pan/zoom via graphicsLayer) is captured, not just
                // translation. Mirrors WaypointState.setTargetBoundsFromLocal.
                val topLeft = try {
                    hostCoords.localPositionOf(coords, Offset(canvasRect.left, canvasRect.top))
                } catch (_: IllegalArgumentException) {
                    return@snapshotFlow null
                }
                val bottomRight = try {
                    hostCoords.localPositionOf(coords, Offset(canvasRect.right, canvasRect.bottom))
                } catch (_: IllegalArgumentException) {
                    return@snapshotFlow null
                }
                val hostRect = Rect(
                    left = minOf(topLeft.x, bottomRight.x),
                    top = minOf(topLeft.y, bottomRight.y),
                    right = maxOf(topLeft.x, bottomRight.x),
                    bottom = maxOf(topLeft.y, bottomRight.y),
                )

                val canvasInRoot = coords.boundsInRoot()
                val hostInRoot = hostCoords.boundsInRoot()
                val visible = canvasInRoot.overlaps(hostInRoot) &&
                    hostRect.width > 1f && hostRect.height > 1f

                CanvasTargetSnapshot(hostRect, visible)
            }.collect { snapshot ->
                if (snapshot == null) return@collect
                if (snapshot.visible) {
                    state.setTargetBounds(currentKey, hostId, snapshot.bounds)
                } else {
                    state.clearTargetBoundsKeepingHost(currentKey)
                }
            }
        }
    }

    this.onGloballyPositioned { coords ->
        canvasCoords = coords
        tick++
    }
}

private data class CanvasTargetSnapshot(
    val bounds: Rect,
    val visible: Boolean,
)
