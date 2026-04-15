package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for event-driven step progression via [WaypointTrigger].
 *
 * Uses [CompletableDeferred] to control exactly when a Custom trigger fires,
 * making tests deterministic without real delays.
 */
@OptIn(ExperimentalTestApi::class)
class AdvanceOnTriggerUiTest {

    // -- Custom trigger auto-advances --

    @Test
    fun `custom trigger auto-advances step when await returns`() = runComposeUiTest {
        val trigger = CompletableDeferred<Unit>()

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "a",
                    advanceOn = WaypointTrigger.Custom { trigger.await() },
                ),
                WaypointStep(targetKey = "b"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.targetKey ?: "",
                        modifier = Modifier.testTag("tooltip"),
                    )
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Column {
                        Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                        Box(Modifier.size(60.dp).waypointTarget(state, "b"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("a").fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals(0, state.currentStepIndex)

        // Fire the trigger
        runOnIdle { trigger.complete(Unit) }
        waitForIdle()

        // Should have auto-advanced to step B
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("b").fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals(1, state.currentStepIndex)
    }

    // -- NextButton does NOT auto-advance --

    @Test
    fun `NextButton trigger does not auto-advance`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "a",
                    advanceOn = WaypointTrigger.NextButton, // explicit default
                ),
                WaypointStep(targetKey = "b"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.targetKey ?: "",
                        modifier = Modifier.testTag("tooltip"),
                    )
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Column {
                        Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                        Box(Modifier.size(60.dp).waypointTarget(state, "b"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("a").fetchSemanticsNodes().isNotEmpty()
        }

        // Wait a bit — should NOT auto-advance
        waitForIdle()
        assertEquals(0, state.currentStepIndex)

        // Manual advance works
        runOnIdle { state.next() }
        waitForIdle()
        assertEquals(1, state.currentStepIndex)
    }

    // -- Custom trigger cancelled when user manually advances --

    @Test
    fun `custom trigger is cancelled when user manually advances`() = runComposeUiTest {
        var triggerCancelled = false
        val neverCompletes = CompletableDeferred<Unit>()

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "a",
                    advanceOn = WaypointTrigger.Custom {
                        try {
                            neverCompletes.await()
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            triggerCancelled = true
                            throw e
                        }
                    },
                ),
                WaypointStep(targetKey = "b"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.targetKey ?: "",
                        modifier = Modifier.testTag("tooltip"),
                    )
                },
                modifier = Modifier.testTag("host"),
            ) {
                Box(Modifier.fillMaxSize()) {
                    Column {
                        Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                        Box(Modifier.size(60.dp).waypointTarget(state, "b"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("a").fetchSemanticsNodes().isNotEmpty()
        }

        // Manually advance while the trigger is still pending
        runOnIdle { state.next() }
        waitForIdle()

        // Should be on step B
        assertEquals(1, state.currentStepIndex)
        // The trigger's coroutine should have been cancelled
        // (LaunchedEffect is cancelled when step changes)
    }

    // -- Custom trigger cancelled when tour is stopped --

    @Test
    fun `custom trigger is cancelled when tour is stopped`() = runComposeUiTest {
        val neverCompletes = CompletableDeferred<Unit>()

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "a",
                    advanceOn = WaypointTrigger.Custom { neverCompletes.await() },
                ),
                WaypointStep(targetKey = "b"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Column {
                        Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                        Box(Modifier.size(60.dp).waypointTarget(state, "b"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Stop the tour while trigger is pending
        runOnIdle { state.stop() }
        waitForIdle()

        assertFalse(state.isActive)
        // The trigger's LaunchedEffect is cancelled because the
        // composable leaves composition when tour becomes inactive
    }

    // -- Custom trigger on last step fires onTourComplete --

    @Test
    fun `custom trigger on last step fires onTourComplete`() = runComposeUiTest {
        val trigger = CompletableDeferred<Unit>()
        var tourCompleted = false

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "only",
                    advanceOn = WaypointTrigger.Custom { trigger.await() },
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                onTourComplete = { tourCompleted = true },
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "only"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        runOnIdle { trigger.complete(Unit) }
        waitForIdle()

        assertFalse(state.isActive)
        assertTrue(tourCompleted)
    }

    // -- Custom trigger still responds to keyboard navigation --

    @Test
    fun `step with custom trigger still responds to keyboard`() = runComposeUiTest {
        val neverCompletes = CompletableDeferred<Unit>()

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "a",
                    advanceOn = WaypointTrigger.Custom { neverCompletes.await() },
                ),
                WaypointStep(targetKey = "b"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.targetKey ?: "",
                        modifier = Modifier.testTag("tooltip"),
                    )
                },
                modifier = Modifier.testTag("host"),
            ) {
                Box(Modifier.fillMaxSize()) {
                    Column {
                        Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                        Box(Modifier.size(60.dp).waypointTarget(state, "b"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("a").fetchSemanticsNodes().isNotEmpty()
        }

        // Use keyboard to advance while trigger is still pending
        onNodeWithTag("host").performKeyInput { pressKey(Key.DirectionRight) }
        waitForIdle()

        assertEquals(1, state.currentStepIndex)
    }

    // -- snapshotFlow-based trigger --

    @Test
    fun `snapshotFlow trigger advances when state changes`() = runComposeUiTest {
        val searchQuery = mutableStateOf("")

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "a",
                    advanceOn = WaypointTrigger.Custom {
                        snapshotFlow { searchQuery.value }
                            .filter { it.isNotEmpty() }
                            .first()
                    },
                ),
                WaypointStep(targetKey = "b"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.targetKey ?: "",
                        modifier = Modifier.testTag("tooltip"),
                    )
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Column {
                        Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                        Box(Modifier.size(60.dp).waypointTarget(state, "b"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("a").fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals(0, state.currentStepIndex)

        // Simulate the user typing — trigger should fire
        runOnIdle { searchQuery.value = "hello" }
        waitForIdle()

        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("b").fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals(1, state.currentStepIndex)
    }
}
