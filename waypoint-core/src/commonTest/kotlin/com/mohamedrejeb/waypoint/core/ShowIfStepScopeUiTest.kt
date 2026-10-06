package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * [StepScope] progress values must be computed over showIf-visible steps so
 * "X of Y" progress and the Finish button stay correct with hidden steps.
 */
@OptIn(ExperimentalTestApi::class)
class ShowIfStepScopeUiTest {

    private fun runScopeTest(
        steps: List<WaypointStep<String>>,
        block: androidx.compose.ui.test.ComposeUiTest.(WaypointState<String>, () -> StepScope?) -> Unit,
    ) = runComposeUiTest {
        val state = WaypointState(steps = steps)
        var latestScope: StepScope? = null

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { scope ->
                    latestScope = scope
                    BasicText("Tooltip")
                },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "a"))
                    Box(Modifier.size(100.dp).waypointTarget(state, "b"))
                    Box(Modifier.size(100.dp).waypointTarget(state, "c"))
                }
            }
        }

        block(state) { latestScope }
    }

    @Test
    fun `hidden trailing step makes the previous step the last one`() = runScopeTest(
        steps = listOf(
            WaypointStep(targetKey = "a"),
            WaypointStep(targetKey = "b"),
            WaypointStep(targetKey = "c", showIf = { false }),
        ),
    ) { state, scope ->
        runOnIdle { state.start() }
        runOnIdle { state.next() }
        waitForIdle()

        val current = scope()
        assertNotNull(current)
        assertEquals(1, current.currentStepIndex)
        assertEquals(2, current.currentStepNumber)
        assertEquals(2, current.totalSteps, "hidden step must not count toward totalSteps")
        assertTrue(current.isLastStep, "step b is the last visible step")
        assertFalse(current.isFirstStep)

        // Advancing past the last visible step completes the tour.
        runOnIdle { state.next() }
        waitForIdle()
        assertFalse(state.isActive)
        assertEquals(WaypointEndReason.Completed, state.lastEndReason)
    }

    @Test
    fun `hidden leading step makes the second step the first one`() = runScopeTest(
        steps = listOf(
            WaypointStep(targetKey = "a", showIf = { false }),
            WaypointStep(targetKey = "b"),
            WaypointStep(targetKey = "c"),
        ),
    ) { state, scope ->
        runOnIdle { state.start() }
        waitForIdle()

        val current = scope()
        assertNotNull(current)
        assertEquals(1, current.currentStepIndex, "start skips the hidden first step")
        assertEquals(1, current.currentStepNumber)
        assertEquals(2, current.totalSteps)
        assertTrue(current.isFirstStep)
        assertFalse(current.isLastStep)
    }

    @Test
    fun `all steps visible reports full counts`() = runScopeTest(
        steps = listOf(
            WaypointStep(targetKey = "a"),
            WaypointStep(targetKey = "b"),
            WaypointStep(targetKey = "c"),
        ),
    ) { state, scope ->
        runOnIdle { state.start() }
        waitForIdle()

        val current = scope()
        assertNotNull(current)
        assertEquals(1, current.currentStepNumber)
        assertEquals(3, current.totalSteps)
        assertTrue(current.isFirstStep)
        assertFalse(current.isLastStep)
    }
}
