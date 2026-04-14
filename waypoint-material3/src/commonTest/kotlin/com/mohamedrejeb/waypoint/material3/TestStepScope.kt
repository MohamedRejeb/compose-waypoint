package com.mohamedrejeb.waypoint.material3

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
    override val onNext: () -> Unit = {},
    override val onPrevious: () -> Unit = {},
    override val onSkip: () -> Unit = {},
    override val onClose: () -> Unit = {},
) : StepScope
