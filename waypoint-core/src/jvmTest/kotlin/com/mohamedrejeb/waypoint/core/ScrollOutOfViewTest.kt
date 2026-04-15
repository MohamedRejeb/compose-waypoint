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
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests that the highlight disappears when the target is scrolled
 * out of view, instead of sticking to the viewport edge.
 *
 * Bug: When a user scrolls during a tour step, the highlight (spotlight,
 * border, pulse) stays stuck at the top or bottom of the visible area
 * with a collapsed/minimum height, instead of disappearing or scrolling
 * out of view with the target.
 *
 * Root cause: `onGloballyPositioned` reports clamped bounds when the
 * target is partially or fully outside the visible viewport. The
 * highlight composable draws at these clamped bounds, producing a
 * thin strip stuck at the edge.
 *
 * Expected: when the target scrolls fully out of view, the highlight
 * should not be visible at all.
 */
@OptIn(ExperimentalTestApi::class)
class ScrollOutOfViewTest {

    // Fixed: degenerate bounds unregistered in WaypointTargetModifier
    @Test
    fun `spotlight disappears when target is scrolled out of view`() = runComposeUiTest {
        val scrollState = ScrollState(0)

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = HighlightStyle.Spotlight(
                        shape = SpotlightShape.RoundedRect(cornerRadius = 8.dp),
                        overlayColor = Color.Black,
                        overlayAlpha = 0.7f,
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
                        .background(Color.White)
                        .verticalScroll(scrollState)
                        .testTag("container"),
                ) {
                    // Target at the top of the scrollable content
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.White)
                            .waypointTarget(state, "target"),
                    )
                    // Large spacer to make content scrollable
                    Spacer(Modifier.height(2000.dp))
                }
            }
        }

        // Start tour — target is visible
        runOnIdle { state.start() }
        waitForIdle()

        // Scroll the target completely out of view (scroll down 500px)
        runOnIdle { scrollState.dispatchRawDelta(500f) }
        waitForIdle()

        // Capture the visible area and check the top edge
        // If the spotlight is stuck, there'll be a dark strip at the top
        val image = onNodeWithTag("container").captureToImage()
        val pixelMap = image.toPixelMap()

        // Sample pixels along the top edge (y=5) — should all be white (no highlight)
        val topEdgeSamples = (10 until image.width step 20).map { x ->
            val pixel = pixelMap[x.coerceIn(0, image.width - 1), 5]
            val brightness = (pixel.red + pixel.green + pixel.blue) / 3f
            x to brightness
        }

        val darkPixelsAtTop = topEdgeSamples.filter { (_, brightness) -> brightness < 0.5f }

        assertTrue(
            darkPixelsAtTop.size <= 2, // allow 1-2 for edge artifacts
            buildString {
                appendLine("Highlight stuck at top edge after scrolling target out of view!")
                appendLine("${darkPixelsAtTop.size} of ${topEdgeSamples.size} samples are dark at y=5")
                appendLine("Dark pixels: ${darkPixelsAtTop.map { (x, b) -> "x=$x brightness=${"%.2f".format(b)}" }}")
                appendLine()
                appendLine("The highlight should disappear when the target is scrolled out of the visible area.")
            },
        )
    }

    // Verify the border highlight target is unregistered when scrolled out.
    // Uses state-based verification instead of pixel sampling (which has
    // layer capture issues with captureToImage on sibling composables).
    @Test
    fun `border highlight target unregistered when scrolled out of view`() = runComposeUiTest {
        val scrollState = ScrollState(0)

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = HighlightStyle.Border(
                        color = Color.Red,
                        shape = SpotlightShape.RoundedRect(cornerRadius = 8.dp),
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
                        .background(Color.White)
                        .verticalScroll(scrollState),
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.White)
                            .waypointTarget(state, "target"),
                    )
                    Spacer(Modifier.height(2000.dp))
                }
            }
        }

        runOnIdle { state.start() }
        waitForIdle()

        // Target should be registered when visible
        assertTrue(
            state.currentTargetBounds != null,
            "Target should have bounds when visible",
        )

        // Scroll target out of view
        runOnIdle { scrollState.dispatchRawDelta(500f) }
        waitForIdle()

        // Target should be unregistered (no bounds = no highlight rendered)
        assertTrue(
            state.currentTargetBounds == null,
            "Target bounds should be null after scrolling out of view (unregistered)",
        )
    }

    // Fixed: degenerate bounds unregistered in WaypointTargetModifier
    @Test
    fun `spotlight stuck at bottom when scrolling target above viewport`() = runComposeUiTest {
        // Target is at the bottom of content; scroll UP so it goes below the viewport
        val scrollState = ScrollState(0)

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = HighlightStyle.Spotlight(
                        overlayColor = Color.Black,
                        overlayAlpha = 0.7f,
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
                        .background(Color.White)
                        .verticalScroll(scrollState)
                        .testTag("container"),
                ) {
                    Spacer(Modifier.height(2000.dp))
                    // Target at the very bottom
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.White)
                            .waypointTarget(state, "target"),
                    )
                }
            }
        }

        // Scroll to the bottom so target is visible, then start
        runOnIdle { scrollState.dispatchRawDelta(2000f) }
        waitForIdle()

        runOnIdle { state.start() }
        waitForIdle()

        // Now scroll back to top — target goes out of view at the bottom
        runOnIdle { scrollState.dispatchRawDelta(-2000f) }
        waitForIdle()

        val image = onNodeWithTag("container").captureToImage()
        val pixelMap = image.toPixelMap()

        // Sample bottom edge — should be white (no stuck highlight)
        val bottomY = (image.height - 5).coerceIn(0, image.height - 1)
        val bottomEdgeSamples = (10 until image.width step 20).map { x ->
            val pixel = pixelMap[x.coerceIn(0, image.width - 1), bottomY]
            val brightness = (pixel.red + pixel.green + pixel.blue) / 3f
            x to brightness
        }

        val darkPixelsAtBottom = bottomEdgeSamples.filter { (_, brightness) -> brightness < 0.5f }

        assertTrue(
            darkPixelsAtBottom.size <= 2,
            buildString {
                appendLine("Highlight stuck at bottom edge after scrolling target above viewport!")
                appendLine("${darkPixelsAtBottom.size} of ${bottomEdgeSamples.size} dark pixels at y=$bottomY")
            },
        )
    }
}
