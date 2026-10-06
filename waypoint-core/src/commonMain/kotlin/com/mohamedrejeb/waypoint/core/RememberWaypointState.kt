package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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
 * **The steps are built once per set of [keys].** [builder] runs when the
 * state is first remembered and again whenever one of the [keys] changes; it
 * is not run on ordinary recomposition. Everything it captures is frozen until
 * the next rebuild:
 *
 * - `title` and `description` are plain strings. A value resolved in
 *   composition (for example with `stringResource`) does not follow a later
 *   locale or configuration change. Resolve such strings where they are shown
 *   instead, inside the step's `content { }` or the host's `tooltipContent`,
 *   which recompose normally.
 * - Lambdas (`showIf`, `onEnter`, `onExit`, `beforeShow`, `advanceOn`) keep the
 *   variables they captured on that first run. Capture state holders and read
 *   them inside the lambda (`showIf { viewModel.isPremium }`), or wrap changing
 *   callbacks with `rememberUpdatedState`, rather than capturing a value that
 *   was read in composition.
 *
 * To have the steps follow such a value, pass it as a key:
 *
 * ```kotlin
 * val locale = Locale.current
 * val state = rememberWaypointState(locale) {
 *     step(Targets.Search) { title = stringResource(Res.string.tour_search) }
 * }
 * ```
 *
 * When a key changes the steps are rebuilt and swapped into the same state:
 * a running tour keeps its step index and its active and paused flags, the
 * index is clamped if the new list is shorter and the tour stops if it is
 * empty, and the current step is entered again so a running `beforeShow` or
 * `advanceOn` from the old lambdas is cancelled and the new one takes over.
 * The step's `onEnter`/`onExit` do not fire for the swap.
 *
 * **Saved state.** The state is kept with `rememberSaveable`: the current step
 * index and the active and paused flags survive configuration changes and
 * process death, and a tour that was running resumes on the same step. Target
 * bounds and the step lambdas are not saved, they are re-registered and
 * re-created on the next composition. If the steps depend on app state that is
 * not restored after process death (an editor whose content the tour assumed,
 * a sheet that was open), check those preconditions when the screen comes back
 * and call [WaypointState.stop] when they are gone, otherwise the restored tour
 * waits on a step whose target will never appear.
 *
 * @param keys values whose change rebuilds the steps; none means build once
 * @param tourId optional identifier for analytics tracking and persistence
 * @param analytics optional analytics tracker for tour events
 * @param persistence optional persistence for remembering tour completion
 * @param builder DSL block to configure steps, run once per set of [keys]
 */
@Composable
public fun <K> rememberWaypointState(
    vararg keys: Any?,
    tourId: String? = null,
    analytics: WaypointAnalytics? = null,
    persistence: WaypointPersistence? = null,
    builder: WaypointStepBuilder<K>.() -> Unit,
): WaypointState<K> {
    val steps = remember(*keys) { WaypointStepBuilder<K>().apply(builder).build() }
    val state = rememberSaveable(
        saver = waypointStateSaver(steps, tourId, analytics, persistence),
    ) {
        WaypointState(steps, tourId = tourId, analytics = analytics, persistence = persistence)
    }
    // A key change produced a new list: swap it into the existing state.
    SideEffect {
        if (state.steps !== steps) state.replaceSteps(steps)
    }
    return state
}

/**
 * Creates and remembers a [WaypointState] from a pre-built list of steps.
 *
 * The returned state survives Android configuration changes (rotation, theme, etc.)
 * via [rememberSaveable]. Only primitive tour state (step index, active, paused) is
 * saved; target coordinates and lambdas are re-registered after recomposition.
 *
 * **The first [steps] list is the one that is used.** A different list passed
 * on a later recomposition is ignored, so texts and lambdas in it do not
 * update. Use the keyed builder overload to have steps follow a value, or key
 * the call site, which creates a new state.
 *
 * @param steps the steps of the tour, read once
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
