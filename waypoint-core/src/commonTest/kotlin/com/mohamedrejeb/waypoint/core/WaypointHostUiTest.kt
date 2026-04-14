package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class WaypointHostUiTest {

    private fun twoStepState() = WaypointState(
        steps = listOf(
            WaypointStep(targetKey = "first", title = "Step One", description = "First description"),
            WaypointStep(targetKey = "second", title = "Step Two", description = "Second description"),
        ),
    )

    // -- Overlay Visibility --

    @Test
    fun `overlay not shown when tour inactive`() = runComposeUiTest {
        val state = twoStepState()

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.size(100.dp).waypointTarget(state, "first"))
            }
        }

        waitForIdle()
        onNodeWithTag("tooltip").assertDoesNotExist()
    }

    @Test
    fun `tooltip shown when tour starts`() = runComposeUiTest {
        val state = twoStepState()

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.title ?: "",
                        modifier = Modifier.testTag("tooltip-title"),
                    )
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                }
            }
        }

        runOnIdle { state.start() }

        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step One").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("tooltip-title").assertTextEquals("Step One")
    }

    // -- Tooltip Content --

    @Test
    fun `tooltip displays step title and description`() = runComposeUiTest {
        val state = twoStepState()

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    Column {
                        BasicText(
                            text = state.currentStep?.title ?: "",
                            modifier = Modifier.testTag("title"),
                        )
                        BasicText(
                            text = state.currentStep?.description ?: "",
                            modifier = Modifier.testTag("desc"),
                        )
                    }
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                }
            }
        }

        runOnIdle { state.start() }

        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step One").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("title").assertTextEquals("Step One")
        onNodeWithTag("desc").assertTextEquals("First description")
    }

    // -- Navigation --

    @Test
    fun `next advances to next step`() = runComposeUiTest {
        val state = twoStepState()

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.title ?: "",
                        modifier = Modifier.testTag("title"),
                    )
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column {
                        Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                        Box(Modifier.size(100.dp).waypointTarget(state, "second"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step One").fetchSemanticsNodes().isNotEmpty()
        }

        runOnIdle { state.next() }
        waitForIdle()

        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step Two").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("title").assertTextEquals("Step Two")
    }

    @Test
    fun `previous goes back to prior step`() = runComposeUiTest {
        val state = twoStepState()

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.title ?: "",
                        modifier = Modifier.testTag("title"),
                    )
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column {
                        Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                        Box(Modifier.size(100.dp).waypointTarget(state, "second"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step One").fetchSemanticsNodes().isNotEmpty()
        }

        runOnIdle { state.next() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step Two").fetchSemanticsNodes().isNotEmpty()
        }

        runOnIdle { state.previous() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step One").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("title").assertTextEquals("Step One")
    }

    @Test
    fun `stop hides tooltip`() = runComposeUiTest {
        val state = twoStepState()

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip visible", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip visible").fetchSemanticsNodes().isNotEmpty()
        }

        runOnIdle { state.stop() }
        waitForIdle()

        onNodeWithTag("tooltip").assertDoesNotExist()
    }

    @Test
    fun `next on last step completes tour`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "only")),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "only"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        runOnIdle { state.next() }
        waitForIdle()

        assertFalse(state.isActive)
        onNodeWithTag("tooltip").assertDoesNotExist()
    }

    // -- Custom Tooltip Content --

    @Test
    fun `custom step content renders instead of host tooltip`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "custom",
                    content = { _ ->
                        BasicText("Custom Content", Modifier.testTag("custom-tooltip"))
                    },
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Default Content", Modifier.testTag("default-tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "custom"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Custom Content").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("custom-tooltip").assertIsDisplayed()
        onNodeWithTag("default-tooltip").assertDoesNotExist()
    }

    // -- StepScope Properties --

    @Test
    fun `step scope provides correct step info`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a"),
                WaypointStep(targetKey = "b"),
                WaypointStep(targetKey = "c"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { scope, _ ->
                    Column {
                        BasicText("index:${scope.currentStepIndex}", Modifier.testTag("index"))
                        BasicText("total:${scope.totalSteps}", Modifier.testTag("total"))
                        BasicText("first:${scope.isFirstStep}", Modifier.testTag("first"))
                        BasicText("last:${scope.isLastStep}", Modifier.testTag("last"))
                    }
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column {
                        Box(Modifier.size(50.dp).waypointTarget(state, "a"))
                        Box(Modifier.size(50.dp).waypointTarget(state, "b"))
                        Box(Modifier.size(50.dp).waypointTarget(state, "c"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("index:0").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("index").assertTextEquals("index:0")
        onNodeWithTag("total").assertTextEquals("total:3")
        onNodeWithTag("first").assertTextEquals("first:true")
        onNodeWithTag("last").assertTextEquals("last:false")

        // Move to last step
        runOnIdle { state.next() }
        waitForIdle()
        runOnIdle { state.next() }
        waitForIdle()

        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("index:2").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("index").assertTextEquals("index:2")
        onNodeWithTag("first").assertTextEquals("first:false")
        onNodeWithTag("last").assertTextEquals("last:true")
    }

    // -- Pause/Resume in UI --

    @Test
    fun `pause hides tooltip, resume shows it again`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "target")),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Visible", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "target"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Visible").fetchSemanticsNodes().isNotEmpty()
        }

        runOnIdle { state.pause() }
        waitForIdle()
        onNodeWithTag("tooltip").assertDoesNotExist()

        runOnIdle { state.resume() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Visible").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("tooltip").assertIsDisplayed()
    }

    // -- Highlight Style Variants --

    /**
     * Helper: create a host with a given highlight style, start the tour,
     * and verify the tooltip appears (tooltip should show regardless of highlight style).
     */
    private fun runHighlightStyleTest(
        highlightStyle: HighlightStyle,
        block: androidx.compose.ui.test.ComposeUiTest.() -> Unit = {},
    ) = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = highlightStyle,
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip Text", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "target"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip Text").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("tooltip").assertIsDisplayed()

        block()
    }

    @Test
    fun `HighlightStyle Default resolves to Spotlight`() {
        assertTrue(HighlightStyle.Default is HighlightStyle.Spotlight)
    }

    @Test
    fun `tooltip shows with Spotlight highlight`() = runHighlightStyleTest(
        highlightStyle = HighlightStyle.Spotlight(),
    )

    @Test
    fun `tooltip shows with Pulse highlight`() = runHighlightStyleTest(
        highlightStyle = HighlightStyle.Pulse(color = Color.Blue),
    )

    @Test
    fun `tooltip shows with Border highlight`() = runHighlightStyleTest(
        highlightStyle = HighlightStyle.Border(color = Color.Red),
    )

    @Test
    fun `tooltip shows with Ripple highlight`() = runHighlightStyleTest(
        highlightStyle = HighlightStyle.Ripple(color = Color.Green),
    )

    @Test
    fun `tooltip shows with None highlight`() = runHighlightStyleTest(
        highlightStyle = HighlightStyle.None,
    )

    @Test
    fun `tooltip shows with Custom highlight`() = runHighlightStyleTest(
        highlightStyle = HighlightStyle.Custom { _, _ -> },
    )

    @Test
    fun `step-level highlightStyle overrides host-level`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = HighlightStyle.None, // step overrides host
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                highlightStyle = HighlightStyle.Spotlight(), // host default
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "target"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Tooltip is visible (no highlight, but tooltip still shows)
        onNodeWithTag("tooltip").assertIsDisplayed()
        // Tour is active
        assertTrue(state.isActive)
    }

    @Test
    fun `host-level highlightStyle used when step uses default`() = runComposeUiTest {
        // Step uses HighlightStyle.Default → should resolve to host's Pulse
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "target"), // default highlightStyle
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                highlightStyle = HighlightStyle.Pulse(color = Color.Magenta),
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "target"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Tour renders — tooltip visible means the host dispatched to Pulse (no crash)
        onNodeWithTag("tooltip").assertIsDisplayed()
    }
}
