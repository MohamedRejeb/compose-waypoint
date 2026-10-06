package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Creates a [WaypointSequenceState] that orchestrates [tours] in the given order.
 *
 * Each tour keeps its own [WaypointState] lifecycle, the sequence only
 * decides which tour is active at any moment. The sequence itself is not
 * saved via `rememberSaveable`: its [WaypointSequenceState.activeIndex] is
 * rebuilt from each tour's own (saved) `isActive` value on construction.
 *
 * @param tours the tours to orchestrate in order
 */
@Composable
public fun rememberWaypointSequenceState(
    vararg tours: WaypointState<*>,
): WaypointSequenceState {
    val toursList = tours.toList()
    return remember(toursList) { WaypointSequenceState(toursList) }
}
