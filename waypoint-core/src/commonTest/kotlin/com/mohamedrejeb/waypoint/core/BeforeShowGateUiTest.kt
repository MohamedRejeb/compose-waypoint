package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The beforeShow contract: a step's highlight and tooltip are not shown until
 * its gate returns, also when the target is already on screen. A gate that
 * returns without suspending never hides such a target.
 *
 * Also covers [WaypointState.isStepVisible], which reports the same thing to
 * the app.
 */
@OptIn(ExperimentalTestApi::class)
class BeforeShowGateUiTest {

    /** Every value [WaypointState.isStepHeld] and [WaypointState.isStepVisible] took, in order. */
    private class Timeline {
        val held = mutableListOf<Boolean>()
        val visible = mutableListOf<Boolean>()
    }

    private fun ComposeUiTest.setTourContent(state: WaypointState<String>): Timeline {
        val timeline = Timeline()
        setContent {
            LaunchedEffect(Unit) {
                snapshotFlow { state.isStepHeld }.collect { timeline.held += it }
            }
            LaunchedEffect(Unit) {
                snapshotFlow { state.isStepVisible }.collect { timeline.visible += it }
            }
            WaypointHost(
                state = state,
                tooltipContent = { scope -> BasicText("tip-${scope.title}") },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.align(Alignment.TopStart).size(80.dp).waypointTarget(state, "a"))
                    Box(Modifier.align(Alignment.BottomEnd).size(80.dp).waypointTarget(state, "b"))
                }
            }
        }
        waitForIdle()
        return timeline
    }

    private fun ComposeUiTest.isTipShown(title: String): Boolean =
        onAllNodesWithText("tip-$title").fetchSemanticsNodes().isNotEmpty()

    private fun ComposeUiTest.awaitTip(title: String) {
        waitUntil(timeoutMillis = 3000) { isTipShown(title) }
        waitForIdle()
    }

    @Test
    fun `suspending gate hides a step whose target is already visible`() = runComposeUiTest {
        val gate = CompletableDeferred<Unit>()
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "A"),
                WaypointStep(targetKey = "b", title = "B", beforeShow = { gate.await() }),
            ),
        )
        setTourContent(state)

        runOnIdle { state.start() }
        awaitTip("A")
        assertTrue(state.isStepVisible)

        runOnIdle { state.next() }
        waitForIdle()

        // Both targets are laid out, yet nothing of step B is shown while the
        // gate is pending, and step A is gone.
        assertFalse(isTipShown("B"))
        assertFalse(isTipShown("A"))
        assertTrue(state.isStepHeld)
        assertFalse(state.isStepVisible)
        assertEquals(1, state.currentStepIndex)

        runOnIdle { gate.complete(Unit) }
        awaitTip("B")
        assertTrue(state.isStepVisible)
        assertFalse(state.isStepHeld)
    }

    @Test
    fun `gate that delays keeps the step hidden for that long`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "A"),
                WaypointStep(targetKey = "b", title = "B", beforeShow = { delay(300) }),
            ),
        )
        setTourContent(state)

        runOnIdle { state.start() }
        awaitTip("A")

        mainClock.autoAdvance = false
        runOnIdle { state.next() }
        mainClock.advanceTimeBy(150)
        assertFalse(isTipShown("B"), "step B must stay hidden while its gate delays")
        assertFalse(state.isStepVisible)

        mainClock.advanceTimeBy(400)
        mainClock.autoAdvance = true
        awaitTip("B")
        assertTrue(state.isStepVisible)
    }

    @Test
    fun `gate that does not suspend never hides an already visible target`() = runComposeUiTest {
        var gateRuns = 0
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "A"),
                WaypointStep(targetKey = "b", title = "B", beforeShow = { gateRuns++ }),
            ),
        )
        val timeline = setTourContent(state)

        runOnIdle { state.start() }
        awaitTip("A")

        // Step frame by frame through the transition: a tooltip is composed on
        // every single frame, there is no hidden frame in between.
        mainClock.autoAdvance = false
        runOnIdle { state.next() }
        repeat(10) { frame ->
            mainClock.advanceTimeByFrame()
            assertTrue(isTipShown("B"), "step B was hidden on frame $frame")
        }
        mainClock.autoAdvance = true
        waitForIdle()

        assertEquals(1, gateRuns)
        assertFalse(timeline.held.any { it }, "the step was held at some point: ${timeline.held}")
        assertTrue(state.isStepVisible)
    }

    @Test
    fun `step is never reported visible before its gate has completed`() = runComposeUiTest {
        val gate = CompletableDeferred<Unit>()
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "A"),
                WaypointStep(targetKey = "b", title = "B", beforeShow = { gate.await() }),
            ),
        )
        val timeline = setTourContent(state)

        runOnIdle { state.start() }
        awaitTip("A")
        runOnIdle { state.next() }
        waitForIdle()

        // false (inactive), true (step A), false (step B pending). No blip back
        // to true while the gate of step B is still running.
        assertEquals(listOf(false, true, false), timeline.visible)

        runOnIdle { gate.complete(Unit) }
        awaitTip("B")
        assertEquals(listOf(false, true, false, true), timeline.visible)
    }

    @Test
    fun `gate holds back a step without a target`() = runComposeUiTest {
        val gate = CompletableDeferred<Unit>()
        val state = WaypointState(
            steps = listOf(
                WaypointStep<String>(title = "Intro", beforeShow = { gate.await() }),
            ),
        )
        setTourContent(state)

        runOnIdle { state.start() }
        waitForIdle()
        assertFalse(isTipShown("Intro"))
        assertFalse(state.isStepVisible)

        runOnIdle { gate.complete(Unit) }
        awaitTip("Intro")
        assertTrue(state.isStepVisible)
    }

    @Test
    fun `navigating away from a held step shows the next step`() = runComposeUiTest {
        val neverCompletes = CompletableDeferred<Unit>()
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "A", beforeShow = { neverCompletes.await() }),
                WaypointStep(targetKey = "b", title = "B"),
            ),
        )
        setTourContent(state)

        runOnIdle { state.start() }
        waitForIdle()
        assertFalse(isTipShown("A"))

        runOnIdle { state.next() }
        awaitTip("B")
        assertTrue(state.isStepVisible)

        // Going back re-arms the gate of step A.
        runOnIdle { state.previous() }
        waitForIdle()
        assertFalse(isTipShown("A"))
        assertFalse(isTipShown("B"))
        assertFalse(state.isStepVisible)
    }

    @Test
    fun `isStepVisible follows activity and pause and target registration`() = runComposeUiTest {
        var showLate by mutableStateOf(false)
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "A"),
                WaypointStep(targetKey = "late", title = "Late"),
            ),
        )
        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { scope -> BasicText("tip-${scope.title}") },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.size(80.dp).waypointTarget(state, "a"))
                    if (showLate) {
                        Box(Modifier.align(Alignment.BottomEnd).size(80.dp).waypointTarget(state, "late"))
                    }
                }
            }
        }
        waitForIdle()
        assertFalse(state.isStepVisible, "inactive tour")

        runOnIdle { state.start() }
        awaitTip("A")
        assertTrue(state.isStepVisible)

        runOnIdle { state.pause() }
        waitForIdle()
        assertFalse(state.isStepVisible, "paused tour")

        runOnIdle { state.resume() }
        waitForIdle()
        assertTrue(state.isStepVisible)

        runOnIdle { state.next() }
        waitForIdle()
        assertFalse(state.isStepVisible, "target of the step is not on screen yet")

        runOnIdle { showLate = true }
        awaitTip("Late")
        assertTrue(state.isStepVisible)

        runOnIdle { state.stop() }
        waitForIdle()
        assertFalse(state.isStepVisible, "stopped tour")
    }
}
