package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates

/**
 * Central state holder for a Waypoint tour.
 *
 * Create via [rememberWaypointState]. Manages step progression, target coordinate
 * tracking, auto-scrolling, and tour lifecycle.
 *
 * @param K the type of the target key (typically an enum)
 */
@Stable
public class WaypointState<K>(
    /** Immutable list of steps in this tour */
    public val steps: List<WaypointStep<K>>,
    /** Optional tour identifier for analytics */
    public val tourId: String? = null,
    /** Optional analytics tracker */
    public val analytics: WaypointAnalytics? = null,
    /** Optional persistence for remembering tour completion */
    public val persistence: WaypointPersistence? = null,
) {
    /** Index of the current step, or -1 if the tour is not active */
    public var currentStepIndex: Int by mutableStateOf(-1)
        private set

    /** Whether the tour is currently active (showing steps) */
    public var isActive: Boolean by mutableStateOf(false)
        private set

    /** Whether the tour is paused */
    public var isPaused: Boolean by mutableStateOf(false)
        private set

    /** Whether the current step's beforeShow gate has completed */
    internal var isStepReady: Boolean by mutableStateOf(true)
        private set

    /** The current step, or null if the tour is not active */
    public val currentStep: WaypointStep<K>?
        get() = if (currentStepIndex in steps.indices) steps[currentStepIndex] else null

    /** Registered target coordinates, keyed by target key */
    internal val targetCoordinates = mutableStateMapOf<K, Rect>()

    /** BringIntoViewRequesters for auto-scrolling targets into view */
    internal val bringIntoViewRequesters = mutableMapOf<K, BringIntoViewRequester>()

    /**
     * LayoutCoordinates of the WaypointHost Box.
     * Used to compute target bounds relative to the host rather than the window root,
     * which fixes coordinate misalignment when the host is inside a Dialog or Sheet.
     */
    internal var hostCoordinates: LayoutCoordinates? = null

    internal fun setStepReady(ready: Boolean) {
        isStepReady = ready
    }

    /** Whether this tour has been completed (requires tourId and persistence) */
    public val hasCompleted: Boolean
        get() {
            val id = tourId ?: return false
            return persistence?.isCompleted(id) ?: false
        }

    /** The bounds of the current step's target, or null if not available */
    public val currentTargetBounds: Rect?
        get() {
            val step = currentStep ?: return null
            return targetCoordinates[step.targetKey]
        }

    // -- Navigation --

    /** Start the tour from the first visible step */
    public fun start() {
        if (isActive) return
        if (hasCompleted) return
        val firstIndex = resolveNextVisibleStep(fromIndex = -1, direction = 1) ?: return
        isActive = true
        isPaused = false
        analytics?.onTourStarted(tourId, steps.size)
        transitionTo(firstIndex)
    }

    /** Advance to the next visible step, or complete the tour if on the last */
    public fun next() {
        if (!isActive || isPaused) return
        val nextIndex = resolveNextVisibleStep(fromIndex = currentStepIndex, direction = 1)
        if (nextIndex != null) {
            transitionTo(nextIndex)
        } else {
            complete()
        }
    }

    /** Go back to the previous visible step */
    public fun previous() {
        if (!isActive || isPaused) return
        val prevIndex = resolveNextVisibleStep(fromIndex = currentStepIndex, direction = -1)
        if (prevIndex != null) {
            transitionTo(prevIndex)
        }
    }

    /** Jump to a specific step by index */
    public fun goTo(index: Int) {
        if (!isActive || isPaused) return
        if (index !in steps.indices) return
        if (steps[index].showIf?.invoke() == false) return
        transitionTo(index)
    }

    /** Jump to a specific step by target key */
    public fun goTo(key: K) {
        val index = steps.indexOfFirst { it.targetKey == key }
        if (index >= 0) goTo(index)
    }

    /** Stop/cancel the tour */
    public fun stop() {
        if (!isActive && !isPaused) return
        val cancelledAtIndex = currentStepIndex
        val exitingStep = currentStep
        isActive = false
        isPaused = false
        currentStepIndex = -1
        isStepReady = true
        exitingStep?.onExit?.invoke()
        analytics?.onTourCancelled(tourId, cancelledAtIndex, steps.size)
    }

    /** Pause the tour (hide overlay, preserve state) */
    public fun pause() {
        if (!isActive || isPaused) return
        isPaused = true
    }

    /** Resume the tour from where it was paused */
    public fun resume() {
        if (!isPaused) return
        isPaused = false
    }

    // -- Auto-scroll --

    /**
     * Scrolls the current step's target into view using [BringIntoViewRequester].
     * This propagates through nested scroll containers automatically.
     *
     * Call this before showing the highlight/tooltip for a step.
     */
    internal suspend fun scrollCurrentTargetIntoView() {
        val step = currentStep ?: return
        val requester = bringIntoViewRequesters[step.targetKey] ?: return
        requester.bringIntoView()
    }

    // -- Target registration (called by Modifier.waypointTarget) --

    internal fun registerTarget(key: K, bounds: Rect) {
        targetCoordinates[key] = bounds
    }

    internal fun registerBringIntoViewRequester(key: K, requester: BringIntoViewRequester) {
        bringIntoViewRequesters[key] = requester
    }

    internal fun unregisterTarget(key: K) {
        targetCoordinates.remove(key)
        bringIntoViewRequesters.remove(key)
    }

    // -- Internal --

    private fun complete() {
        val exitingStep = currentStep
        val exitingIndex = currentStepIndex
        exitingStep?.onExit?.invoke()
        analytics?.onStepCompleted(tourId, exitingIndex, exitingStep?.targetKey)
        isActive = false
        isPaused = false
        currentStepIndex = -1
        isStepReady = true
        analytics?.onTourCompleted(tourId, steps.size)
        val id = tourId
        if (id != null) persistence?.markCompleted(id)
    }

    /** Force-marks this tour as completed in persistence */
    public fun markCompleted() {
        val id = tourId ?: return
        persistence?.markCompleted(id)
    }

    /** Resets the completion state so the tour can be shown again */
    public fun resetCompletion() {
        val id = tourId ?: return
        persistence?.reset(id)
    }

    private fun transitionTo(newIndex: Int) {
        val exitingStep = currentStep
        val exitingIndex = currentStepIndex
        exitingStep?.onExit?.invoke()
        if (exitingIndex >= 0) {
            analytics?.onStepCompleted(tourId, exitingIndex, exitingStep?.targetKey)
        }
        currentStepIndex = newIndex
        val enteringStep = currentStep
        enteringStep?.onEnter?.invoke()
        analytics?.onStepViewed(tourId, newIndex, enteringStep?.targetKey)
        // Gate highlight/tooltip if step has beforeShow
        if (steps[newIndex].beforeShow != null) {
            isStepReady = false
        }
    }

    /**
     * Starting from [fromIndex], search in [direction] (+1 or -1) for the next
     * step whose [WaypointStep.showIf] condition passes (or is null).
     */
    private fun resolveNextVisibleStep(fromIndex: Int, direction: Int): Int? {
        var i = fromIndex + direction
        while (i in steps.indices) {
            if (steps[i].showIf?.invoke() != false) return i
            i += direction
        }
        return null
    }
}
