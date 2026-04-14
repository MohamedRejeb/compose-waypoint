package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Creates and remembers a [WaypointState] configured via the DSL [builder].
 *
 * ```kotlin
 * val state = rememberWaypointState {
 *     step(MyTargets.SearchBar) {
 *         title = "Search"
 *         description = "Find anything fast"
 *     }
 *     step(MyTargets.AddButton) {
 *         title = "Create"
 *         description = "Add a new item"
 *     }
 * }
 * ```
 */
@Composable
public fun <K> rememberWaypointState(
    builder: WaypointStepBuilder<K>.() -> Unit,
): WaypointState<K> {
    return remember {
        val steps = WaypointStepBuilder<K>().apply(builder).build()
        WaypointState(steps)
    }
}

/**
 * Creates and remembers a [WaypointState] from a pre-built list of steps.
 */
@Composable
public fun <K> rememberWaypointState(
    steps: List<WaypointStep<K>>,
): WaypointState<K> {
    return remember(steps) {
        WaypointState(steps)
    }
}
