package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Stable

/**
 * Scope provided to tooltip content composables.
 * Exposes everything a tooltip needs: the step's texts, where the tooltip sits,
 * progress among the visible steps, and navigation.
 *
 * Implemented by the library only; it may gain members in any release.
 */
@Stable
public interface StepScope {
    /** Title configured on the current step */
    public val title: String?

    /** Description configured on the current step */
    public val description: String?

    /** Resolved side of the target the tooltip sits on, null for a step without a target */
    public val placement: ResolvedPlacement?

    /** Index of the current step in [WaypointState.steps] (0-based, includes hidden steps) */
    public val currentStepIndex: Int

    /** 1-based position of the current step among currently-visible steps, for "X of Y" progress */
    public val currentStepNumber: Int

    /** Total number of currently-visible steps in the tour (steps whose showIf passes) */
    public val totalSteps: Int

    /** Whether this is the first visible step */
    public val isFirstStep: Boolean

    /** Whether this is the last visible step */
    public val isLastStep: Boolean

    /**
     * True when the step's [WaypointStep.advanceOn] is armed for this visit
     * (the step has one and was not entered through `previous()`), so the
     * tour moves on by itself. A tooltip can hide its Next button in that case.
     */
    public val advancesAutomatically: Boolean

    /** Navigate to the next step (or complete the tour if on the last step) */
    public fun next()

    /** Navigate to the previous step */
    public fun previous()

    /** Skip the rest of the tour (cancels it) */
    public fun skip()
}

internal data class StepScopeImpl(
    private val state: WaypointState<*>,
    override val title: String?,
    override val description: String?,
    override val placement: ResolvedPlacement?,
    override val currentStepIndex: Int,
    override val currentStepNumber: Int,
    override val totalSteps: Int,
    override val isFirstStep: Boolean,
    override val isLastStep: Boolean,
    override val advancesAutomatically: Boolean,
) : StepScope {
    override fun next() {
        state.next()
    }

    override fun previous() {
        state.previous()
    }

    override fun skip() {
        state.stop()
    }
}
