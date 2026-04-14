package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for auto-scroll behavior: targets inside scrollable containers
 * should be scrolled into view when their step activates.
 */
@OptIn(ExperimentalTestApi::class)
class AutoScrollUiTest {

    @Test
    fun `off-screen target is scrolled into view when step activates`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "top"),
                WaypointStep(targetKey = "bottom"),
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
                // Container is 300dp tall, content is 2000dp — "bottom" target is off-screen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .waypointTarget(state, "top")
                            .testTag("target-top"),
                    )
                    Spacer(Modifier.height(1800.dp))
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .waypointTarget(state, "bottom")
                            .testTag("target-bottom"),
                    )
                }
            }
        }

        // Start on first step (top — already visible)
        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("top").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("tooltip").assertIsDisplayed()

        // Advance to second step — "bottom" is off-screen and should be scrolled into view
        runOnIdle { state.next() }
        waitForIdle()

        // The tooltip should appear for "bottom" after auto-scroll
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithText("bottom").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("tooltip").assertIsDisplayed()
    }

    @Test
    fun `visible target does not cause unnecessary scroll`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "visible"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                // Target is fully visible — no scrolling needed
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .waypointTarget(state, "visible")
                            .testTag("target"),
                    )
                    Spacer(Modifier.height(1000.dp))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Target is visible, tooltip shows — no crash, no weird scroll behavior
        onNodeWithTag("tooltip").assertIsDisplayed()
        onNodeWithTag("target").assertIsDisplayed()
    }

    @Test
    fun `navigating back scrolls previous target into view`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "top"),
                WaypointStep(targetKey = "bottom"),
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .waypointTarget(state, "top")
                            .testTag("target-top"),
                    )
                    Spacer(Modifier.height(1800.dp))
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .waypointTarget(state, "bottom")
                            .testTag("target-bottom"),
                    )
                }
            }
        }

        // Start → top
        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("top").fetchSemanticsNodes().isNotEmpty()
        }

        // Next → bottom (scrolls down)
        runOnIdle { state.next() }
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithText("bottom").fetchSemanticsNodes().isNotEmpty()
        }

        // Previous → top (should scroll back up)
        runOnIdle { state.previous() }
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithText("top").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("tooltip").assertIsDisplayed()
    }

    @Test
    fun `tour works without scrollable container`() = runComposeUiTest {
        // Verify auto-scroll is a no-op when there's no scroll container
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "target"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .waypointTarget(state, "target"),
                    )
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // No scroll container, no crash — tooltip still shows
        onNodeWithTag("tooltip").assertIsDisplayed()
        assertTrue(state.isActive)
    }
}
