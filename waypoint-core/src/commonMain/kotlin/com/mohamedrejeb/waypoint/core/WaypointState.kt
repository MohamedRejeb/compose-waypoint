package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect

/**
 * Central state holder for a Waypoint tour.
 *
 * Create via [rememberWaypointState]. Manages step progression, target coordinate
 * tracking, and tour lifecycle.
 *
 * @param K the type of the target key (typically an enum)
 */
@Stable
public class WaypointState<K>(
    /** Immutable list of steps in this tour */
    public val steps: List<WaypointStep<K>>,
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

    /** The current step, or null if the tour is not active */
    public val currentStep: WaypointStep<K>?
        get() = if (currentStepIndex in steps.indices) steps[currentStepIndex] else null

    /** Registered target coordinates, keyed by target key */
    internal val targetCoordinates = mutableStateMapOf<K, Rect>()

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
        val firstIndex = resolveNextVisibleStep(fromIndex = -1, direction = 1) ?: return
        isActive = true
        isPaused = false
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
        val exitingStep = currentStep
        isActive = false
        isPaused = false
        currentStepIndex = -1
        exitingStep?.onExit?.invoke()
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

    // -- Target registration (called by Modifier.waypointTarget) --

    internal fun registerTarget(key: K, bounds: Rect) {
        targetCoordinates[key] = bounds
    }

    internal fun unregisterTarget(key: K) {
        targetCoordinates.remove(key)
    }

    // -- Internal --

    private fun complete() {
        val exitingStep = currentStep
        exitingStep?.onExit?.invoke()
        isActive = false
        isPaused = false
        currentStepIndex = -1
    }

    private fun transitionTo(newIndex: Int) {
        val exitingStep = currentStep
        exitingStep?.onExit?.invoke()
        currentStepIndex = newIndex
        currentStep?.onEnter?.invoke()
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
