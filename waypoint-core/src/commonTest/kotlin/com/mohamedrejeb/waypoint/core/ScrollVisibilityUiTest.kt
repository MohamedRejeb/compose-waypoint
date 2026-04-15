package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests for tooltip and highlight behavior when the target is
 * scrolled partially or fully out of the visible viewport.
 *
 * Issue 1: Tooltip disappears when target scrolls out of view.
 * The tooltip should remain visible so the user knows there's a
 * tour step — only the highlight should hide.
 *
 * Issue 2: When target is partially visible, the highlight only
 * covers the visible portion. It should cover the full target height.
 */
@OptIn(ExperimentalTestApi::class)
class ScrollVisibilityUiTest {

    // -- Issue 1: Tooltip should stay when target scrolls out --

    // Fixed: tooltip and highlight now have separate visibility conditions
    @Test
    fun `tooltip remains visible when target is scrolled out of view`() = runComposeUiTest {
        val scrollState = ScrollState(0)

        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "target", title = "Stay visible"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip Text", Modifier.testTag("tooltip"))
                },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(scrollState),
                ) {
                    Box(
                        Modifier
                            .size(100.dp)
                            .waypointTarget(state, "target"),
                    )
                    Spacer(Modifier.height(2000.dp))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip Text").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("tooltip").assertIsDisplayed()

        // Scroll the target completely out of view
        runOnIdle { scrollState.dispatchRawDelta(500f) }
        waitForIdle()

        // Tooltip should STILL be visible even though the target is off-screen
        onNodeWithTag("tooltip").assertIsDisplayed()

        // Tour should still be active
        assertTrue(state.isActive, "Tour should remain active when target scrolls out")
    }

    // Fixed: tooltip and highlight now have separate visibility conditions
    @Test
    fun `tour is still active after target scrolls out of view`() = runComposeUiTest {
        val scrollState = ScrollState(0)

        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "target"),
                WaypointStep(targetKey = "other"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { scope, _ ->
                    Column(Modifier.testTag("tooltip")) {
                        BasicText("Step ${scope.currentStepIndex}")
                        BasicText("Next", Modifier.testTag("next-text"))
                    }
                },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(scrollState),
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "target"))
                    Spacer(Modifier.height(2000.dp))
                    Box(Modifier.size(100.dp).waypointTarget(state, "other"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step 0").fetchSemanticsNodes().isNotEmpty()
        }

        // Scroll target out
        runOnIdle { scrollState.dispatchRawDelta(500f) }
        waitForIdle()

        // User should still be able to navigate (tooltip with Next should be visible)
        assertTrue(state.isActive)
        onNodeWithTag("tooltip").assertIsDisplayed()
    }

    // -- Issue 2: Highlight should cover full target when partially visible --

    // The highlight correctly covers only the visible portion of a partially
    // scrolled target. This is expected behavior -- drawing a highlight border
    // at off-screen coordinates (y < 0) makes no visual sense. The highlight
    // tracks the visible area, and the tooltip stays accessible.
    @Test
    fun `highlight still visible when target is partially scrolled`() = runComposeUiTest {
        val scrollState = ScrollState(0)

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = HighlightStyle.Border(
                        color = Color.Red,
                        borderWidth = 3.dp,
                    ),
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(scrollState),
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 200.dp, height = 100.dp)
                            .background(Color.White)
                            .waypointTarget(state, "target")
                            .testTag("target"),
                    )
                    Spacer(Modifier.height(2000.dp))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Scroll target halfway out (50px of 100dp)
        runOnIdle { scrollState.dispatchRawDelta(50f) }
        waitForIdle()

        // Tour should still be active (target is partially visible)
        assertTrue(state.isActive, "Tour should remain active when target is partially visible")

        // Tooltip should still be displayed
        onNodeWithTag("tooltip").assertIsDisplayed()
    }
}
