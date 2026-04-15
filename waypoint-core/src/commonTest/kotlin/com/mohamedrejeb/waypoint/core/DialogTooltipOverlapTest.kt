package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests that the tooltip is positioned correctly relative to the target
 * when WaypointHost is nested inside an offset container (Dialog, Sheet,
 * or any layout that offsets the host from the window root).
 *
 * After the `localBoundingBoxOf` fix, target bounds are host-relative.
 * The spotlight Canvas draws correctly because it fills the host Box.
 * But the tooltip Popup receives host-relative bounds and interprets
 * them as window-relative, causing the tooltip to appear at the wrong
 * position — typically covering the target.
 *
 * The test framework's Dialog doesn't create a real window offset, so we
 * reproduce the mismatch by placing WaypointHost inside a Box with large
 * top padding. This creates the same coordinate space mismatch: target
 * bounds are small (host-relative) while the actual window position is
 * large (host is offset).
 */
@OptIn(ExperimentalTestApi::class)
class DialogTooltipOverlapTest {

    // Fixed: tooltip now receives window-relative bounds via host positionInWindow offset
    @Test
    fun `tooltip does not overlap target when host is offset from root`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "title",
                    placement = TooltipPlacement.Bottom,
                ),
            ),
        )

        setContent {
            // Outer container with large top padding — simulates Dialog offset
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.padding(top = 300.dp)) {
                    WaypointHost(
                        state = state,
                        tooltipContent = { _, _ ->
                            BasicText("Tooltip Content", Modifier.testTag("tooltip"))
                        },
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            BasicText(
                                text = "Target Title",
                                modifier = Modifier
                                    .waypointTarget(state, "title")
                                    .testTag("target"),
                            )
                            Spacer(Modifier.height(200.dp))
                        }
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip Content").fetchSemanticsNodes().isNotEmpty()
        }

        val targetBounds = onNodeWithTag("target").getBoundsInRoot()
        val tooltipBounds = onNodeWithTag("tooltip").getBoundsInRoot()

        // Bottom placement: tooltip top should be >= target bottom (below the target)
        // Allow small tolerance for spacing
        val tooltipIsBelow = tooltipBounds.top >= targetBounds.bottom - 2.dp

        assertTrue(
            tooltipIsBelow,
            buildString {
                appendLine("Tooltip overlaps/covers the target when host is offset!")
                appendLine("Target: top=${targetBounds.top}, bottom=${targetBounds.bottom}")
                appendLine("Tooltip: top=${tooltipBounds.top}, bottom=${tooltipBounds.bottom}")
                appendLine("Expected tooltip.top >= target.bottom (Bottom placement)")
                appendLine()
                appendLine("The tooltip Popup is using host-relative targetBounds (small y)")
                appendLine("but should use window-relative coordinates (large y, accounting for host offset).")
            },
        )
    }

    // Fixed: tooltip now receives window-relative bounds via host positionInWindow offset
    @Test
    fun `tooltip does not overlap target when host is offset horizontally`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    placement = TooltipPlacement.End,
                ),
            ),
        )

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.padding(start = 200.dp, top = 100.dp)) {
                    WaypointHost(
                        state = state,
                        tooltipContent = { _, _ ->
                            BasicText("Tooltip", Modifier.testTag("tooltip"))
                        },
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .waypointTarget(state, "target")
                                    .testTag("target"),
                            )
                            Spacer(Modifier.height(200.dp))
                        }
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        val targetBounds = onNodeWithTag("target").getBoundsInRoot()
        val tooltipBounds = onNodeWithTag("tooltip").getBoundsInRoot()

        // End placement: tooltip left should be >= target right (to the right of target)
        val tooltipIsRight = tooltipBounds.left >= targetBounds.right - 2.dp

        assertTrue(
            tooltipIsRight,
            buildString {
                appendLine("Tooltip overlaps target when host is offset horizontally!")
                appendLine("Target: left=${targetBounds.left}, right=${targetBounds.right}")
                appendLine("Tooltip: left=${tooltipBounds.left}, right=${tooltipBounds.right}")
                appendLine("Expected tooltip.left >= target.right (End placement)")
            },
        )
    }
}
