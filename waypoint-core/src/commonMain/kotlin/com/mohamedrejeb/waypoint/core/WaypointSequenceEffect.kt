package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

/**
 * Auto-advances [state] when the current tour finishes.
 *
 * Observes [WaypointState.isActive] on the sequence's current tour.
 * When the tour transitions to inactive:
 *  - if it ended with [WaypointEndReason.Completed], [WaypointSequenceState.advance] is called
 *  - otherwise the tour was cancelled and [WaypointSequenceState.stop] is called
 *
 * Tours do not need persistence for this to work; the decision is based on how
 * the run ended, not on persisted completion state.
 *
 * Call this once in the composition that owns the sequence. Without it, you
 * need to call [WaypointSequenceState.advance] manually (e.g., from
 * `onTourComplete` on each host).
 *
 * @param state the sequence to observe
 */
@Composable
public fun WaypointSequenceEffect(state: WaypointSequenceState) {
    val tour = state.currentTour
    LaunchedEffect(tour) {
        if (tour == null) return@LaunchedEffect
        snapshotFlow { tour.isActive }
            .dropWhile { !it }   // wait until the tour becomes active
            .filter { !it }       // then wait for the next inactive transition
            .first()
        if (tour.lastEndReason == WaypointEndReason.Completed) {
            state.advance()
        } else {
            state.stop()
        }
    }
}
