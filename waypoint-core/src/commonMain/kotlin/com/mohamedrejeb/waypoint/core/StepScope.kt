package com.mohamedrejeb.waypoint.core

/**
 * Scope provided to custom tooltip content composables.
 * Exposes tour state and navigation controls.
 */
public interface StepScope {
    /** Index of the current step (0-based) */
    public val currentStepIndex: Int

    /** Total number of visible steps in the tour */
    public val totalSteps: Int

    /** Whether this is the first visible step */
    public val isFirstStep: Boolean

    /** Whether this is the last visible step */
    public val isLastStep: Boolean

    /** Navigate to the next step (or complete the tour if on the last step) */
    public val onNext: () -> Unit

    /** Navigate to the previous step */
    public val onPrevious: () -> Unit

    /** Skip/cancel the tour */
    public val onSkip: () -> Unit

    /** Close/cancel the tour (alias for onSkip) */
    public val onClose: () -> Unit
}

internal data class StepScopeImpl(
    override val currentStepIndex: Int,
    override val totalSteps: Int,
    override val isFirstStep: Boolean,
    override val isLastStep: Boolean,
    override val onNext: () -> Unit,
    override val onPrevious: () -> Unit,
    override val onSkip: () -> Unit,
    override val onClose: () -> Unit,
) : StepScope
