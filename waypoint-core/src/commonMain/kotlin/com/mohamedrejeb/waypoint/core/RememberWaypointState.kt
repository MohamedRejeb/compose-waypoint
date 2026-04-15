package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Creates and remembers a [WaypointState] configured via the DSL [builder].
 *
 * The returned state survives Android configuration changes (rotation, theme, etc.)
 * via [rememberSaveable]. Only primitive tour state (step index, active, paused) is
 * saved; target coordinates and lambdas are re-registered after recomposition.
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
 * @param tourId optional identifier for analytics tracking and persistence
 * @param analytics optional analytics tracker for tour events
 * @param persistence optional persistence for remembering tour completion
 * @param builder DSL block to configure steps
 */
@Composable
public fun <K> rememberWaypointState(
    tourId: String? = null,
    analytics: WaypointAnalytics? = null,
    persistence: WaypointPersistence? = null,
    builder: WaypointStepBuilder<K>.() -> Unit,
): WaypointState<K> {
    val steps = remember { WaypointStepBuilder<K>().apply(builder).build() }
    return rememberSaveable(
        saver = waypointStateSaver(steps, tourId, analytics, persistence),
    ) {
        WaypointState(steps, tourId = tourId, analytics = analytics, persistence = persistence)
    }
}

/**
 * Creates and remembers a [WaypointState] from a pre-built list of steps.
 *
 * The returned state survives Android configuration changes (rotation, theme, etc.)
 * via [rememberSaveable]. Only primitive tour state (step index, active, paused) is
 * saved; target coordinates and lambdas are re-registered after recomposition.
 *
 * @param tourId optional identifier for analytics tracking and persistence
 * @param analytics optional analytics tracker for tour events
 * @param persistence optional persistence for remembering tour completion
 */
@Composable
public fun <K> rememberWaypointState(
    steps: List<WaypointStep<K>>,
    tourId: String? = null,
    analytics: WaypointAnalytics? = null,
    persistence: WaypointPersistence? = null,
): WaypointState<K> {
    return rememberSaveable(
        saver = waypointStateSaver(steps, tourId, analytics, persistence),
    ) {
        WaypointState(steps, tourId = tourId, analytics = analytics, persistence = persistence)
    }
}

private fun <K> waypointStateSaver(
    steps: List<WaypointStep<K>>,
    tourId: String?,
    analytics: WaypointAnalytics?,
    persistence: WaypointPersistence?,
): Saver<WaypointState<K>, List<Any>> = Saver(
    save = { state ->
        listOf(
            state.currentStepIndex,
            state.isActive,
            state.isPaused,
        )
    },
    restore = { saved ->
        WaypointState(steps, tourId = tourId, analytics = analytics, persistence = persistence).also {
            it.restoreState(
                savedStepIndex = saved[0] as Int,
                savedIsActive = saved[1] as Boolean,
                savedIsPaused = saved[2] as Boolean,
            )
        }
    },
)
