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
 *
 * @param tourId optional identifier for analytics tracking
 * @param analytics optional analytics tracker for tour events
 * @param builder DSL block to configure steps
 */
@Composable
public fun <K> rememberWaypointState(
    tourId: String? = null,
    analytics: WaypointAnalytics? = null,
    builder: WaypointStepBuilder<K>.() -> Unit,
): WaypointState<K> {
    return remember {
        val steps = WaypointStepBuilder<K>().apply(builder).build()
        WaypointState(steps, tourId = tourId, analytics = analytics)
    }
}

/**
 * Creates and remembers a [WaypointState] from a pre-built list of steps.
 *
 * @param tourId optional identifier for analytics tracking
 * @param analytics optional analytics tracker for tour events
 */
@Composable
public fun <K> rememberWaypointState(
    steps: List<WaypointStep<K>>,
    tourId: String? = null,
    analytics: WaypointAnalytics? = null,
): WaypointState<K> {
    return remember(steps) {
        WaypointState(steps, tourId = tourId, analytics = analytics)
    }
}
