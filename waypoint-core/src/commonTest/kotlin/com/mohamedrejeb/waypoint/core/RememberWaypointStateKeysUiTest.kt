package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * `rememberWaypointState(vararg keys)` rebuilds the steps when a key changes
 * and swaps them into the same state, so a running tour keeps its place while
 * texts and lambdas follow the new build.
 */
@OptIn(ExperimentalTestApi::class)
class RememberWaypointStateKeysUiTest {

    private class Holder {
        var state: WaypointState<String>? = null
    }

    private fun ComposeUiTest.awaitTip(text: String) {
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("tip-$text").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()
    }

    @Test
    fun `titles follow a key change while the step index stays`() = runComposeUiTest {
        var locale by mutableStateOf("en")
        val holder = Holder()
        setContent {
            val state = rememberWaypointState(locale) {
                step("a") { title = if (locale == "en") "Hello" else "Bonjour" }
                step("b") { title = if (locale == "en") "World" else "Monde" }
            }
            holder.state = state
            WaypointHost(state = state, tooltipContent = { scope -> BasicText("tip-${scope.title}") }) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.align(Alignment.TopStart).size(60.dp).waypointTarget(state, "a"))
                    Box(Modifier.align(Alignment.BottomEnd).size(60.dp).waypointTarget(state, "b"))
                }
            }
        }
        val state = assertNotNull(holder.state)
        runOnIdle { state.start() }
        awaitTip("Hello")
        runOnIdle { state.next() }
        awaitTip("World")

        runOnIdle { locale = "fr" }

        awaitTip("Monde")
        assertSame(state, holder.state, "the state instance must survive the key change")
        assertEquals(1, state.currentStepIndex)
        assertTrue(state.isActive)
        assertEquals("Bonjour", state.steps[0].title)
    }

    @Test
    fun `unchanged keys do not rebuild the steps`() = runComposeUiTest {
        var builds = 0
        var unrelated by mutableStateOf(0)
        setContent {
            unrelated // recompose on change
            rememberWaypointState("fixed") {
                builds++
                step("a") { title = "A" }
            }
        }
        waitForIdle()
        runOnIdle { unrelated++ }
        waitForIdle()

        assertEquals(1, builds)
    }

    @Test
    fun `a shorter list clamps the index and an empty list stops the tour`() = runComposeUiTest {
        var count by mutableStateOf(3)
        val holder = Holder()
        setContent {
            val state = rememberWaypointState(count) {
                repeat(count) { i -> step("t$i") { title = "S$i" } }
            }
            holder.state = state
            WaypointHost(state = state, tooltipContent = { scope -> BasicText("tip-${scope.title}") }) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.align(Alignment.TopStart).size(60.dp).waypointTarget(state, "t0"))
                    Box(Modifier.align(Alignment.Center).size(60.dp).waypointTarget(state, "t1"))
                    Box(Modifier.align(Alignment.BottomEnd).size(60.dp).waypointTarget(state, "t2"))
                }
            }
        }
        val state = assertNotNull(holder.state)
        runOnIdle { state.start() }
        awaitTip("S0")
        runOnIdle { state.goToStep(2) }
        awaitTip("S2")

        runOnIdle { count = 1 }
        awaitTip("S0")
        assertEquals(0, state.currentStepIndex, "the index is clamped to the shorter list")
        assertTrue(state.isActive)
        assertTrue(state.isStepVisible)

        runOnIdle { count = 0 }
        waitForIdle()
        assertFalse(state.isActive, "an empty list stops the tour")
        assertEquals(WaypointEndReason.Cancelled, state.lastEndReason)
    }

    @Test
    fun `the old advanceOn is cancelled and the new one is armed`() = runComposeUiTest {
        var version by mutableStateOf(1)
        val oldTrigger = CompletableDeferred<Unit>()
        val newTrigger = CompletableDeferred<Unit>()
        val holder = Holder()
        setContent {
            val state = rememberWaypointState(version) {
                step("a") {
                    title = "A$version"
                    advanceOn { if (version == 1) oldTrigger.await() else newTrigger.await() }
                }
                step("b") { title = "B" }
            }
            holder.state = state
            WaypointHost(state = state, tooltipContent = { scope -> BasicText("tip-${scope.title}") }) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.align(Alignment.TopStart).size(60.dp).waypointTarget(state, "a"))
                    Box(Modifier.align(Alignment.BottomEnd).size(60.dp).waypointTarget(state, "b"))
                }
            }
        }
        val state = assertNotNull(holder.state)
        runOnIdle { state.start() }
        awaitTip("A1")

        runOnIdle { version = 2 }
        awaitTip("A2")

        runOnIdle { oldTrigger.complete(Unit) }
        waitForIdle()
        assertEquals(0, state.currentStepIndex, "the old trigger moved the tour")

        runOnIdle { newTrigger.complete(Unit) }
        awaitTip("B")
        assertEquals(1, state.currentStepIndex)
    }

    @Test
    fun `an inactive tour just takes the new steps`() = runComposeUiTest {
        var locale by mutableStateOf("en")
        val holder = Holder()
        setContent {
            holder.state = rememberWaypointState(locale) {
                step("a") { title = if (locale == "en") "Hello" else "Bonjour" }
            }
        }
        waitForIdle()
        val state = assertNotNull(holder.state)
        assertEquals("Hello", state.steps[0].title)

        runOnIdle { locale = "fr" }
        waitForIdle()

        assertEquals("Bonjour", state.steps[0].title)
        assertFalse(state.isActive)
        assertEquals(-1, state.currentStepIndex)
    }
}
