package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Step effects follow every entry into a step, not just changes of the step
 * index: stopping and starting again within one frame, or re-entering the same
 * index, re-runs the gate and re-arms advanceOn, and work from a previous
 * visit never acts on the current one.
 */
@OptIn(ExperimentalTestApi::class)
class StepRestartUiTest {

    private fun ComposeUiTest.setTour(state: WaypointState<String>) {
        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { scope -> BasicText("tip-${scope.title}") },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(80.dp).waypointTarget(state, "a"))
                    Box(Modifier.align(Alignment.BottomEnd).size(80.dp).waypointTarget(state, "b"))
                }
            }
        }
        waitForIdle()
    }

    private fun ComposeUiTest.isTipShown(title: String): Boolean =
        onAllNodesWithText("tip-$title").fetchSemanticsNodes().isNotEmpty()

    private fun ComposeUiTest.awaitTip(title: String) {
        waitUntil(timeoutMillis = 3000) { isTipShown(title) }
        waitForIdle()
    }

    @Test
    fun `restarting a gated one step tour in one frame runs the gate again`() = runComposeUiTest {
        var gateRuns = 0
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "a", title = "A", beforeShow = { gateRuns++ })),
        )
        setTour(state)

        runOnIdle { state.start() }
        awaitTip("A")
        assertEquals(1, gateRuns)

        runOnIdle {
            state.stop()
            state.start()
        }

        awaitTip("A")
        assertEquals(2, gateRuns)
        assertTrue(state.isStepVisible)
    }

    @Test
    fun `completing a one step tour and starting it again from onTourComplete shows the step`() =
        runComposeUiTest {
            val state = WaypointState(
                steps = listOf(WaypointStep(targetKey = "a", title = "A", beforeShow = { })),
            )
            setContent {
                WaypointHost(
                    state = state,
                    onTourComplete = { state.start() },
                    tooltipContent = { scope -> BasicText("tip-${scope.title}") },
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(80.dp).waypointTarget(state, "a"))
                    }
                }
            }
            runOnIdle { state.start() }
            awaitTip("A")

            runOnIdle { state.next() }

            awaitTip("A")
            assertTrue(state.isActive)
            assertTrue(state.isStepVisible)
        }

    @Test
    fun `an advanceOn from a previous visit does not advance the restarted tour`() = runComposeUiTest {
        val firstVisit = CompletableDeferred<Unit>()
        var visits = 0
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "a",
                    title = "A",
                    advanceOn = {
                        visits++
                        if (visits == 1) firstVisit.await() else CompletableDeferred<Unit>().await()
                    },
                ),
                WaypointStep(targetKey = "b", title = "B"),
            ),
        )
        setTour(state)

        runOnIdle { state.start() }
        awaitTip("A")
        runOnIdle {
            state.stop()
            state.start()
        }
        awaitTip("A")
        assertEquals(2, visits, "the trigger must be re-armed for the new visit")

        // The stale trigger of the first visit completes now.
        runOnIdle { firstVisit.complete(Unit) }
        waitForIdle()

        assertEquals(0, state.currentStepIndex, "a stale trigger moved the tour")
        assertTrue(isTipShown("A"))
    }

    @Test
    fun `a gate that completes right after navigating away does not reveal the new step early`() =
        runComposeUiTest {
            val gateA = CompletableDeferred<Unit>()
            val gateB = CompletableDeferred<Unit>()
            val state = WaypointState(
                steps = listOf(
                    WaypointStep(targetKey = "a", title = "A", beforeShow = { gateA.await() }),
                    WaypointStep(targetKey = "b", title = "B", beforeShow = { gateB.await() }),
                ),
            )
            setTour(state)

            runOnIdle { state.start() }
            waitForIdle()
            assertFalse(isTipShown("A"))

            // Within the same frame: move on, then the old gate finishes.
            runOnIdle {
                state.next()
                gateA.complete(Unit)
            }
            waitForIdle()

            assertEquals(1, state.currentStepIndex)
            assertFalse(isTipShown("B"), "step B was revealed by step A's gate")
            assertFalse(state.isStepVisible)

            runOnIdle { gateB.complete(Unit) }
            awaitTip("B")
        }

    @Test
    fun `restarting the tour snaps the highlight to the first target`() = runComposeUiTest {
        var lastAnimated: Rect? = null
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "A"),
                WaypointStep(targetKey = "b", title = "B"),
            ),
        )
        setContent {
            WaypointHost(
                state = state,
                highlightStyle = HighlightStyle.Custom { _, animated, _ -> lastAnimated = animated },
                tooltipContent = { scope -> BasicText("tip-${scope.title}") },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.align(Alignment.TopStart).size(80.dp).waypointTarget(state, "a"))
                    Box(Modifier.align(Alignment.BottomEnd).size(80.dp).waypointTarget(state, "b"))
                }
            }
        }

        runOnIdle { state.start() }
        awaitTip("A")
        runOnIdle { state.next() }
        awaitTip("B")

        mainClock.autoAdvance = false
        runOnIdle {
            state.stop()
            state.start()
        }
        repeat(3) { mainClock.advanceTimeByFrame() }

        // No glide from b back to a: the highlight is already at a.
        assertEquals(state.currentTargetBounds, lastAnimated)
        mainClock.autoAdvance = true
    }
}
