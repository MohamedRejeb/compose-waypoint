package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/**
 * Orchestrates a linear sequence of [WaypointState]s.
 *
 * The sequence does not own the tours, each [WaypointState] keeps its own
 * steps, host, and persistence. The sequence tracks which tour is currently
 * active and coordinates start/advance/stop.
 *
 * Cross-session progress is inherited from each tour's [WaypointPersistence]:
 * tours that report [WaypointState.hasCompleted] are skipped on [start] and
 * [advance]. Use [reset] to clear completion and restart the sequence.
 *
 * On construction, [activeIndex] is derived from the tours' current
 * [WaypointState.isActive] values, so a sequence recreated after a
 * configuration change automatically picks up the tour that was active
 * before (since each tour survives via `rememberSaveable`). The sequence
 * itself intentionally does not use `rememberSaveable`, saving its
 * [activeIndex] independently would fight with the tours' own saved state.
 *
 * Pair with [WaypointSequenceEffect] inside a composition to auto-advance
 * when the current tour finishes.
 *
 * @param tours ordered list of tours to run
 */
@Stable
public class WaypointSequenceState @PublishedApi internal constructor(
    /** Ordered list of tours orchestrated by this sequence */
    public val tours: List<WaypointState<*>>,
) {
    /** Index of the active tour in [tours], or -1 if the sequence is inactive */
    public var activeIndex: Int by mutableIntStateOf(-1)
        private set

    init {
        // Derive activeIndex from tours so a sequence rebuilt after a
        // configuration change picks up the tour that rememberSaveable
        // restored as active.
        val idx = tours.indexOfFirst { it.isActive }
        if (idx >= 0) activeIndex = idx
    }

    /** The currently active tour, or null if inactive */
    public val currentTour: WaypointState<*>? get() = tours.getOrNull(activeIndex)

    /** True when a tour in the sequence is currently active */
    public val isActive: Boolean get() = activeIndex in tours.indices

    /** True when every tour in [tours] reports [WaypointState.hasCompleted] */
    public val isCompleted: Boolean get() = tours.isNotEmpty() && tours.all { it.hasCompleted }

    /**
     * Start the sequence from the first incomplete tour. No-op if the
     * sequence is already active or every tour has been completed.
     */
    public fun start() {
        if (isActive) return
        val firstIncomplete = tours.indexOfFirst { !it.hasCompleted }
        if (firstIncomplete < 0) return
        activeIndex = firstIncomplete
        tours[firstIncomplete].start()
    }

    /**
     * Advance to the next incomplete tour after the current one. When no
     * more incomplete tours remain, the sequence becomes inactive.
     */
    public fun advance() {
        if (!isActive) return
        var i = activeIndex + 1
        while (i < tours.size) {
            if (!tours[i].hasCompleted) {
                activeIndex = i
                tours[i].start()
                return
            }
            i++
        }
        // No more incomplete tours, sequence finished.
        activeIndex = -1
    }

    /** Stop the current tour and halt the sequence. */
    public fun stop() {
        val tour = currentTour
        // Flip activeIndex first so WaypointSequenceEffect observing the
        // tour.isActive transition does not re-trigger via the effect.
        activeIndex = -1
        tour?.stop()
    }

    /** Explicitly jump to a specific tour in the list if it is not yet completed. */
    public fun goTo(tour: WaypointState<*>) {
        val index = tours.indexOfFirst { it === tour }
        if (index < 0) return
        if (tours[index].hasCompleted) return
        val previous = currentTour
        activeIndex = index
        if (previous != null && previous !== tour) previous.stop()
        tour.start()
    }

    /**
     * Reset every tour's persistence completion state and halt the sequence.
     * After this, [start] will begin from the first tour.
     */
    public fun reset() {
        val tour = currentTour
        activeIndex = -1
        tour?.stop()
        tours.forEach { it.resetCompletion() }
    }
}
