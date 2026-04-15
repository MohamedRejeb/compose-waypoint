package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for multi-element highlight: a step can highlight multiple targets
 * simultaneously while the tooltip anchors to the primary target.
 */
@OptIn(ExperimentalTestApi::class)
class MultiElementHighlightUiTest {

    // -- Tooltip anchored to primary target --

    @Test
    fun `tooltip is anchored to primary target not additional targets`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "primary",
                    placement = TooltipPlacement.Bottom,
                    additionalTargets = listOf("secondary"),
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Multi Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    // Primary at top-left
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .waypointTarget(state, "primary")
                            .testTag("primary"),
                    )
                    // Secondary far away at bottom-right
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(60.dp)
                            .waypointTarget(state, "secondary")
                            .testTag("secondary"),
                    )
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Multi Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        val primaryBounds = onNodeWithTag("primary").getBoundsInRoot()
        val secondaryBounds = onNodeWithTag("secondary").getBoundsInRoot()
        val tooltipBounds = onNodeWithTag("tooltip").getBoundsInRoot()

        // Compare using raw dp values
        val tooltipCenterY = (tooltipBounds.top.value + tooltipBounds.bottom.value) / 2f
        val primaryCenterY = (primaryBounds.top.value + primaryBounds.bottom.value) / 2f
        val secondaryCenterY = (secondaryBounds.top.value + secondaryBounds.bottom.value) / 2f

        // Tooltip should be closer to primary than to secondary
        val distToPrimary = abs(tooltipCenterY - primaryCenterY)
        val distToSecondary = abs(tooltipCenterY - secondaryCenterY)

        assertTrue(
            distToPrimary < distToSecondary,
            "Tooltip should be anchored to primary target. " +
                "Dist to primary: $distToPrimary, dist to secondary: $distToSecondary",
        )
    }

    // -- Unregistered additional target is skipped --

    @Test
    fun `unregistered additional target is silently skipped`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "primary",
                    additionalTargets = listOf("nonexistent"),
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "primary"))
                    // "nonexistent" target is NOT registered — no waypointTarget for it
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Tour works fine — no crash from missing additional target
        onNodeWithTag("tooltip").assertIsDisplayed()
        assertTrue(state.isActive)
    }

    // -- Empty additionalTargets works (existing behavior) --

    @Test
    fun `empty additionalTargets works normally`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "solo",
                    additionalTargets = emptyList(), // explicit empty
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Solo Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "solo"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Solo Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("tooltip").assertIsDisplayed()
    }

    // -- Multi-element with different highlight styles --

    @Test
    fun `multi-element works with Spotlight highlight`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "a",
                    highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.Circle),
                    additionalTargets = listOf("b", "c"),
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Multi", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Row {
                        Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                        Spacer(Modifier.width(20.dp))
                        Box(Modifier.size(60.dp).waypointTarget(state, "b"))
                        Spacer(Modifier.width(20.dp))
                        Box(Modifier.size(60.dp).waypointTarget(state, "c"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Multi").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("tooltip").assertIsDisplayed()
        assertTrue(state.isActive)
    }

    @Test
    fun `multi-element works with Border highlight`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "a",
                    highlightStyle = HighlightStyle.Border(
                        color = androidx.compose.ui.graphics.Color.Red,
                    ),
                    additionalTargets = listOf("b"),
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Border Multi", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Column {
                        Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                        Spacer(Modifier.height(40.dp))
                        Box(Modifier.size(60.dp).waypointTarget(state, "b"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Border Multi").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("tooltip").assertIsDisplayed()
    }

    // -- StepBuilder DSL --

    @Test
    fun `additionalTargets set via builder DSL`() {
        val builder = WaypointStepBuilder<String>()
        builder.step("primary") {
            additionalTargets = listOf("extra1", "extra2")
        }

        val step = builder.build().single()

        assertEquals(listOf("extra1", "extra2"), step.additionalTargets)
    }

    @Test
    fun `additionalTargets defaults to empty in builder`() {
        val builder = WaypointStepBuilder<String>()
        builder.step("primary")

        val step = builder.build().single()

        assertEquals(emptyList(), step.additionalTargets)
    }
}
