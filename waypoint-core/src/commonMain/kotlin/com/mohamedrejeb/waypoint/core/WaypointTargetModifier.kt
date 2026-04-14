package com.mohamedrejeb.waypoint.core

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
 * spotlight overlay and position the tooltip when this target's step is active.
 *
 * ```kotlin
 * Button(
 *     modifier = Modifier.waypointTarget(waypointState, MyTargets.AddButton),
 *     onClick = { /* ... */ }
 * ) { Text("Add") }
 * ```
 *
 * @param state the [WaypointState] managing the tour
 * @param key the target key identifying this composable in the step list
 */
public fun <K> Modifier.waypointTarget(
    state: WaypointState<K>,
    key: K,
): Modifier = composed {
    val currentKey = remember(key) { key }

    DisposableEffect(currentKey) {
        onDispose {
            state.unregisterTarget(currentKey)
        }
    }

    onGloballyPositioned { coordinates ->
        if (coordinates.isAttached) {
            val bounds = coordinates.boundsInRoot()
            if (bounds != Rect.Zero) {
                state.registerTarget(currentKey, bounds)
            }
        }
    }
}
