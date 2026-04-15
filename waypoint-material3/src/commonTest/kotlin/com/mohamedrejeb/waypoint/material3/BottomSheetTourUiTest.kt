package com.mohamedrejeb.waypoint.material3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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

/**
 * Tests that WaypointMaterial3Host works correctly inside a ModalBottomSheet.
 * Bottom sheets are Popup-based containers that render in a separate layer.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
class BottomSheetTourUiTest {

    @Test
    fun `full tour works inside ModalBottomSheet`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "Sheet Step A", description = "First"),
                WaypointStep(targetKey = "b", title = "Sheet Step B", description = "Second"),
            ),
        )
        var completed = false

        setContent {
            MaterialTheme {
                ModalBottomSheet(onDismissRequest = {}) {
                    WaypointMaterial3Host(
                        state = state,
                        onTourComplete = { completed = true },
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
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Sheet Step A").fetchSemanticsNodes().isNotEmpty()
        }

        // Material3 tooltip renders inside the sheet
        onNodeWithText("Sheet Step A").assertIsDisplayed()
        onNodeWithText("First").assertIsDisplayed()
        onNodeWithText("1 of 2").assertIsDisplayed()

        // Click Next → step 2
        onNodeWithText("Next").performClick()
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Sheet Step B").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithText("Sheet Step B").assertIsDisplayed()
        onNodeWithText("Back").assertIsDisplayed()
        onNodeWithText("Finish").assertIsDisplayed()

        // Click Finish → complete
        onNodeWithText("Finish").performClick()
        waitForIdle()

        assertFalse(state.isActive)
        assertTrue(completed)
    }

    @Test
    fun `skip works inside ModalBottomSheet`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "Sheet Step"),
            ),
        )
        var cancelled = false

        setContent {
            MaterialTheme {
                ModalBottomSheet(onDismissRequest = {}) {
                    WaypointMaterial3Host(
                        state = state,
                        onTourCancel = { cancelled = true },
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                        }
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Sheet Step").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithText("Skip").performClick()
        waitForIdle()

        assertFalse(state.isActive)
        assertTrue(cancelled)
    }

    @Test
    fun `themed tooltip works inside ModalBottomSheet`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "Themed Sheet"),
            ),
        )

        setContent {
            MaterialTheme {
                ModalBottomSheet(onDismissRequest = {}) {
                    WaypointMaterial3Theme(
                        colors = WaypointMaterial3Theme.colors(
                            tooltipBackground = androidx.compose.ui.graphics.Color.DarkGray,
                            title = androidx.compose.ui.graphics.Color.White,
                        ),
                    ) {
                        WaypointMaterial3Host(state = state) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                            }
                        }
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Themed Sheet").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithText("Themed Sheet").assertIsDisplayed()
    }
}
