package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Edge case tests for uncommon but important scenarios.
 */
@OptIn(ExperimentalTestApi::class)
class EdgeCaseUiTest {

    // -- Target removed mid-tour --

    @Test
    fun `tour survives when target is removed from composition`() = runComposeUiTest {
        var showTarget by mutableStateOf(true)

        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "removable"),
                WaypointStep(targetKey = "stable"),
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
                        if (showTarget) {
                            Box(
                                Modifier
                                    .size(60.dp)
                                    .waypointTarget(state, "removable")
                                    .testTag("removable"),
                            )
                        }
                        Box(
                            Modifier
                                .size(60.dp)
                                .waypointTarget(state, "stable")
                                .testTag("stable"),
                        )
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("removable").fetchSemanticsNodes().isNotEmpty()
        }

        // Remove the target from composition while its step is active
        runOnIdle { showTarget = false }
        waitForIdle()

        // Tour should still be active (not crash)
        assertTrue(state.isActive)
        // Target bounds should be null since it was unregistered
        assertEquals(null, state.currentTargetBounds)

        // Can still advance to the next step
        runOnIdle { state.next() }
        waitForIdle()

        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("stable").fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals(1, state.currentStepIndex)
    }

    @Test
    fun `tour can be stopped after target removal`() = runComposeUiTest {
        var showTarget by mutableStateOf(true)

        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "removable"),
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
                    if (showTarget) {
                        Box(Modifier.size(60.dp).waypointTarget(state, "removable"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Remove target, then stop
        runOnIdle { showTarget = false }
        waitForIdle()

        runOnIdle { state.stop() }
        waitForIdle()

        assertFalse(state.isActive)
    }

    // -- Long text wrapping --

    @Test
    fun `long title and description wrap within tooltip`() = runComposeUiTest {
        val longTitle = "This is a very long title that should wrap to multiple lines within the tooltip"
        val longDesc = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
            "Ut enim ad minim veniam, quis nostrud exercitation ullamco."

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    title = longTitle,
                    description = longDesc,
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    Column(Modifier.testTag("tooltip")) {
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
                    Box(Modifier.size(60.dp).waypointTarget(state, "target"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText(longTitle).fetchSemanticsNodes().isNotEmpty()
        }

        // Both long texts render without crash
        onNodeWithTag("title").assertIsDisplayed()
        onNodeWithTag("desc").assertIsDisplayed()

        // Tooltip is visible (no overflow causing invisible content)
        onNodeWithTag("tooltip").assertIsDisplayed()
    }

    // -- RTL Layout --

    @Test
    fun `Start placement positions tooltip to the right in RTL`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    placement = TooltipPlacement.Start,
                ),
            ),
        )

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                WaypointHost(
                    state = state,
                    tooltipContent = { _, _ ->
                        BasicText("RTL Tooltip", Modifier.testTag("tooltip"))
                    },
                ) {
                    // Target centered horizontally
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(Modifier.size(60.dp).waypointTarget(state, "target").testTag("target"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("RTL Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        val targetBounds = onNodeWithTag("target").getBoundsInRoot()
        val tooltipBounds = onNodeWithTag("tooltip").getBoundsInRoot()

        // In RTL, Start = right side. Tooltip should be to the right of the target.
        assertTrue(
            tooltipBounds.left >= targetBounds.right,
            "In RTL, Start placement should put tooltip to the RIGHT of target. " +
                "Target right: ${targetBounds.right}, Tooltip left: ${tooltipBounds.left}",
        )
    }

    @Test
    fun `End placement positions tooltip to the left in RTL`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    placement = TooltipPlacement.End,
                ),
            ),
        )

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                WaypointHost(
                    state = state,
                    tooltipContent = { _, _ ->
                        BasicText("RTL Tooltip", Modifier.testTag("tooltip"))
                    },
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(Modifier.size(60.dp).waypointTarget(state, "target").testTag("target"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("RTL Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        val targetBounds = onNodeWithTag("target").getBoundsInRoot()
        val tooltipBounds = onNodeWithTag("tooltip").getBoundsInRoot()

        // In RTL, End = left side. Tooltip should be to the left of the target.
        assertTrue(
            tooltipBounds.right <= targetBounds.left,
            "In RTL, End placement should put tooltip to the LEFT of target. " +
                "Target left: ${targetBounds.left}, Tooltip right: ${tooltipBounds.right}",
        )
    }

    // -- Single step tour --

    @Test
    fun `single step tour has isFirst and isLast both true`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "only")),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { scope, _ ->
                    Column(Modifier.testTag("tooltip")) {
                        BasicText("first:${scope.isFirstStep}", Modifier.testTag("first"))
                        BasicText("last:${scope.isLastStep}", Modifier.testTag("last"))
                        BasicText("total:${scope.totalSteps}", Modifier.testTag("total"))
                    }
                },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "only"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("first:true").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("tooltip").assertIsDisplayed()
        onNodeWithTag("first").assertTextEquals("first:true")
        onNodeWithTag("last").assertTextEquals("last:true")
        onNodeWithTag("total").assertTextEquals("total:1")
    }
}
