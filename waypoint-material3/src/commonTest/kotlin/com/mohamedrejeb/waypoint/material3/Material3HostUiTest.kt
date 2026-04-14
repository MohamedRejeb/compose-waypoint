package com.mohamedrejeb.waypoint.material3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.WaypointStep
import com.mohamedrejeb.waypoint.core.waypointTarget
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class Material3HostUiTest {

    private fun threeStepState() = WaypointState(
        steps = listOf(
            WaypointStep(targetKey = "a", title = "First Step", description = "First desc"),
            WaypointStep(targetKey = "b", title = "Second Step", description = "Second desc"),
            WaypointStep(targetKey = "c", title = "Third Step", description = "Third desc"),
        ),
    )

    private fun waitForTooltip(text: String, block: () -> Unit = {}) {
        // Helper pattern — called inside runComposeUiTest
    }

    // -- Full Tour Flow --

    @Test
    fun `full tour flow with Material3 tooltip`() = runComposeUiTest {
        val state = threeStepState()
        var tourCompleted = false

        setContent {
            MaterialTheme {
                WaypointMaterial3Host(
                    state = state,
                    onTourComplete = { tourCompleted = true },
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column {
                            Box(Modifier.size(80.dp).testTag("target-a").waypointTarget(state, "a"))
                            Box(Modifier.size(80.dp).testTag("target-b").waypointTarget(state, "b"))
                            Box(Modifier.size(80.dp).testTag("target-c").waypointTarget(state, "c"))
                        }
                    }
                }
            }
        }

        // Start tour
        runOnIdle { state.start() }

        // Step 1: First Step
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("First Step").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithText("First Step").assertIsDisplayed()
        onNodeWithText("First desc").assertIsDisplayed()
        onNodeWithText("1 of 3").assertIsDisplayed()
        onNodeWithText("Next").assertIsDisplayed()
        onNodeWithText("Skip").assertIsDisplayed()
        // Back should not be visible on first step
        onNodeWithText("Back").assertDoesNotExist()

        // Click Next → Step 2
        onNodeWithText("Next").performClick()
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Second Step").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithText("Second Step").assertIsDisplayed()
        onNodeWithText("2 of 3").assertIsDisplayed()
        onNodeWithText("Back").assertIsDisplayed()
        onNodeWithText("Next").assertIsDisplayed()

        // Click Next → Step 3 (last)
        onNodeWithText("Next").performClick()
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Third Step").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithText("Third Step").assertIsDisplayed()
        onNodeWithText("3 of 3").assertIsDisplayed()
        onNodeWithText("Finish").assertIsDisplayed()
        onNodeWithText("Next").assertDoesNotExist()

        // Click Finish → tour complete
        onNodeWithText("Finish").performClick()
        waitForIdle()

        assertFalse(state.isActive)
        assertTrue(tourCompleted)
    }

    // -- Back Navigation --

    @Test
    fun `back button returns to previous step`() = runComposeUiTest {
        val state = threeStepState()

        setContent {
            MaterialTheme {
                WaypointMaterial3Host(state = state) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column {
                            Box(Modifier.size(80.dp).waypointTarget(state, "a"))
                            Box(Modifier.size(80.dp).waypointTarget(state, "b"))
                            Box(Modifier.size(80.dp).waypointTarget(state, "c"))
                        }
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("First Step").fetchSemanticsNodes().isNotEmpty()
        }

        // Go forward
        onNodeWithText("Next").performClick()
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Second Step").fetchSemanticsNodes().isNotEmpty()
        }

        // Go back
        onNodeWithText("Back").performClick()
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("First Step").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithText("First Step").assertIsDisplayed()
        onNodeWithText("1 of 3").assertIsDisplayed()
    }

    // -- Skip / Cancel --

    @Test
    fun `skip button cancels tour`() = runComposeUiTest {
        val state = threeStepState()
        var tourCancelled = false

        setContent {
            MaterialTheme {
                WaypointMaterial3Host(
                    state = state,
                    onTourCancel = { tourCancelled = true },
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column {
                            Box(Modifier.size(80.dp).waypointTarget(state, "a"))
                            Box(Modifier.size(80.dp).waypointTarget(state, "b"))
                            Box(Modifier.size(80.dp).waypointTarget(state, "c"))
                        }
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("First Step").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithText("Skip").performClick()
        waitForIdle()

        assertFalse(state.isActive)
        assertTrue(tourCancelled)
    }

    // -- Custom Button Text --

    @Test
    fun `custom button text is displayed`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "only", title = "Only Step"),
            ),
        )

        setContent {
            MaterialTheme {
                WaypointMaterial3Host(
                    state = state,
                    skipText = "Dismiss",
                    finishText = "Done!",
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(Modifier.size(80.dp).waypointTarget(state, "only"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Only Step").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithText("Dismiss").assertIsDisplayed()
        onNodeWithText("Done!").assertIsDisplayed()
    }

    // -- Progress Visibility --

    @Test
    fun `progress hidden when showProgress is false`() = runComposeUiTest {
        val state = threeStepState()

        setContent {
            MaterialTheme {
                WaypointMaterial3Host(
                    state = state,
                    showProgress = false,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column {
                            Box(Modifier.size(80.dp).waypointTarget(state, "a"))
                            Box(Modifier.size(80.dp).waypointTarget(state, "b"))
                            Box(Modifier.size(80.dp).waypointTarget(state, "c"))
                        }
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("First Step").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithText("1 of 3").assertDoesNotExist()
    }
}
