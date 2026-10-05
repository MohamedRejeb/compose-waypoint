package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * advanceOn is armed on every entry except the user's Back navigation
 * (`previous()`). Going back into a step whose condition already holds shows
 * it with manual navigation instead of bouncing the user forward again, while
 * programmatic jumps keep the trigger live.
 */
@OptIn(ExperimentalTestApi::class)
class AdvanceOnDirectionUiTest {

    private class Observed {
        var scope: StepScope? = null
    }

    /** Three steps; the middle one advances as soon as its condition holds (it always does). */
    private fun runDirectionTest(block: ComposeUiTest.(WaypointState<String>, Observed) -> Unit) =
        runComposeUiTest {
            val observed = Observed()
            val state = WaypointState(
                steps = listOf(
                    WaypointStep(targetKey = "a", title = "A"),
                    WaypointStep(targetKey = "b", title = "B", advanceOn = { }),
                    WaypointStep(targetKey = "c", title = "C"),
                ),
            )
            setContent {
                WaypointHost(
                    state = state,
                    tooltipContent = { scope ->
                        observed.scope = scope
                        BasicText("tip-${scope.title}")
                    },
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Box(Modifier.align(Alignment.TopStart).size(60.dp).waypointTarget(state, "a"))
                        Box(Modifier.align(Alignment.Center).size(60.dp).waypointTarget(state, "b"))
                        Box(Modifier.align(Alignment.BottomEnd).size(60.dp).waypointTarget(state, "c"))
                    }
                }
            }
            runOnIdle { state.start() }
            awaitTip("A")
            block(state, observed)
        }

    private fun ComposeUiTest.awaitTip(title: String) {
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("tip-$title").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()
    }

    @Test
    fun `entered forward the step advances automatically`() = runDirectionTest { state, _ ->
        runOnIdle { state.next() }

        awaitTip("C")
        assertEquals(2, state.currentStepIndex)
    }

    @Test
    fun `entered backward the step waits for manual navigation`() = runDirectionTest { state, observed ->
        runOnIdle { state.next() }
        awaitTip("C")

        runOnIdle { state.previous() }
        awaitTip("B")
        mainClock.advanceTimeBy(500)
        waitForIdle()

        assertEquals(1, state.currentStepIndex, "the tour bounced forward again")
        assertFalse(observed.scope?.advancesAutomatically == true)

        runOnIdle { observed.scope?.next() }
        awaitTip("C")
    }

    @Test
    fun `scope reports whether the trigger is armed`() = runDirectionTest { state, observed ->
        assertFalse(observed.scope?.advancesAutomatically == true, "step A has no trigger")

        runOnIdle { state.goToStep(2) }
        awaitTip("C")
        runOnIdle { state.previous() }
        awaitTip("B")
        assertEquals(false, observed.scope?.advancesAutomatically)

        // Leaving and coming back forward re-arms it.
        runOnIdle { state.previous() }
        awaitTip("A")
        runOnIdle { state.next() }
        awaitTip("C")
    }

    @Test
    fun `goToStep to a higher index arms the trigger`() = runDirectionTest { state, _ ->
        runOnIdle { state.goToStep(1) }

        awaitTip("C")
    }

    @Test
    fun `goToStep to a lower index arms the trigger too`() = runDirectionTest { state, observed ->
        runOnIdle { state.goToStep(2) }
        awaitTip("C")

        // The app sends the user back to redo the step: the trigger is live.
        runOnIdle { state.goToStep(1) }

        awaitTip("C")
        assertEquals(2, state.currentStepIndex)
        assertEquals("C", observed.scope?.title)
    }

    @Test
    fun `goTo by key to a lower index arms the trigger too`() = runDirectionTest { state, _ ->
        runOnIdle { state.goToStep(2) }
        awaitTip("C")

        runOnIdle { state.goTo("b") }

        awaitTip("C")
    }

    @Test
    fun `step entered forward reports advancesAutomatically`() = runComposeUiTest {
        var scope: StepScope? = null
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "A", advanceOn = { kotlinx.coroutines.awaitCancellation() }),
            ),
        )
        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { s ->
                    scope = s
                    BasicText("tip-${s.title}")
                },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                }
            }
        }
        runOnIdle { state.start() }
        awaitTip("A")

        assertTrue(scope?.advancesAutomatically == true)
    }
}
