package com.mohamedrejeb.waypoint.material3

import com.mohamedrejeb.waypoint.core.ResolvedPlacement
import com.mohamedrejeb.waypoint.core.StepScope

/**
 * Test-only implementation of [StepScope].
 *
 * [StepScopeImpl] is internal to waypoint-core, so this module's tests
 * need their own implementation of the public interface.
 */
internal data class TestStepScope(
    override val currentStepIndex: Int,
    override val totalSteps: Int,
    override val isFirstStep: Boolean,
    override val isLastStep: Boolean,
    override val currentStepNumber: Int = currentStepIndex + 1,
    override val title: String? = null,
    override val description: String? = null,
    override val placement: ResolvedPlacement? = ResolvedPlacement.Bottom,
    override val advancesAutomatically: Boolean = false,
    val onNext: () -> Unit = {},
    val onPrevious: () -> Unit = {},
    val onSkip: () -> Unit = {},
) : StepScope {
    override fun next() = onNext()
    override fun previous() = onPrevious()
    override fun skip() = onSkip()
}
