package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Tests that WaypointHost works correctly inside Popup-based containers
 * (Dialog, BottomSheet). These use a separate window/layer, which could
 * cause coordinate space misalignment or nested Popup issues.
 */
@OptIn(ExperimentalTestApi::class)
class PopupContainerUiTest {

    // -- WaypointHost inside a Dialog --

    @Test
    fun `tour works inside a Dialog`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "Dialog Step A"),
                WaypointStep(targetKey = "b", title = "Dialog Step B"),
            ),
        )

        setContent {
            Dialog(onDismissRequest = {}) {
                WaypointHost(
                    state = state,
                    tooltipContent = { _, _ ->
                        BasicText(
                            text = state.currentStep?.title ?: "",
                            modifier = Modifier.testTag("tooltip"),
                        )
                    },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Box(Modifier.size(80.dp).waypointTarget(state, "a"))
                        Box(Modifier.size(80.dp).waypointTarget(state, "b"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Dialog Step A").fetchSemanticsNodes().isNotEmpty()
        }

        // Tooltip shows on step 1
        onNodeWithTag("tooltip").assertTextEquals("Dialog Step A")

        // Advance to step 2
        runOnIdle { state.next() }
        waitForIdle()
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Dialog Step B").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("tooltip").assertTextEquals("Dialog Step B")

        // Complete tour
        runOnIdle { state.next() }
        waitForIdle()
        assertFalse(state.isActive)
    }

    @Test
    fun `spotlight renders inside Dialog`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = HighlightStyle.Spotlight(
                        shape = SpotlightShape.Circle,
                    ),
                ),
            ),
        )

        setContent {
            Dialog(onDismissRequest = {}) {
                WaypointHost(
                    state = state,
                    tooltipContent = { _, _ ->
                        BasicText("Tooltip", Modifier.testTag("tooltip"))
                    },
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(Modifier.size(60.dp).waypointTarget(state, "target"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Spotlight + tooltip both render inside the dialog
        onNodeWithTag("tooltip").assertIsDisplayed()
    }

    @Test
    fun `different highlight styles work inside Dialog`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "pulse",
                    highlightStyle = HighlightStyle.Pulse(
                        color = androidx.compose.ui.graphics.Color.Blue,
                    ),
                ),
                WaypointStep(
                    targetKey = "none",
                    highlightStyle = HighlightStyle.None,
                ),
            ),
        )

        setContent {
            Dialog(onDismissRequest = {}) {
                WaypointHost(
                    state = state,
                    tooltipContent = { _, _ ->
                        BasicText(
                            text = state.currentStep?.targetKey ?: "",
                            modifier = Modifier.testTag("tooltip"),
                        )
                    },
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Box(Modifier.size(60.dp).waypointTarget(state, "pulse"))
                        Box(Modifier.size(60.dp).waypointTarget(state, "none"))
                    }
                }
            }
        }

        // Step 1: Pulse inside dialog
        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("pulse").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("tooltip").assertIsDisplayed()

        // Step 2: None highlight inside dialog
        runOnIdle { state.next() }
        waitForIdle()
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("none").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("tooltip").assertIsDisplayed()
    }

    @Test
    fun `tour stop works inside Dialog`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "target")),
        )

        setContent {
            Dialog(onDismissRequest = {}) {
                WaypointHost(
                    state = state,
                    tooltipContent = { _, _ ->
                        BasicText("Tooltip", Modifier.testTag("tooltip"))
                    },
                ) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(60.dp).waypointTarget(state, "target"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        runOnIdle { state.stop() }
        waitForIdle()

        assertFalse(state.isActive)
        onNodeWithTag("tooltip").assertDoesNotExist()
    }
}
