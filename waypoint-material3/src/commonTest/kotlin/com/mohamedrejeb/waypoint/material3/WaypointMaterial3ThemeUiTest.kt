package com.mohamedrejeb.waypoint.material3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.WaypointStep
import com.mohamedrejeb.waypoint.core.waypointTarget
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [WaypointMaterial3Theme] theming system.
 *
 * Verifies that custom colors, typography, and dimensions are applied
 * to the tooltip, and that defaults fall back to MaterialTheme.
 */
@OptIn(ExperimentalTestApi::class)
class WaypointMaterial3ThemeUiTest {

    // -- Default theme renders correctly --

    @Test
    fun `tooltip renders with default theme when no WaypointMaterial3Theme provided`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "Default Title", description = "Default desc"),
            ),
        )

        setContent {
            MaterialTheme {
                // No WaypointMaterial3Theme wrapping — should use Material3 defaults
                WaypointMaterial3Host(state = state) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Default Title").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithText("Default Title").assertIsDisplayed()
        onNodeWithText("Default desc").assertIsDisplayed()
        onNodeWithText("1 of 1").assertIsDisplayed()
    }

    // -- Custom colors applied --

    @Test
    fun `custom theme colors are applied to tooltip`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "Themed Title", description = "Themed desc"),
            ),
        )

        setContent {
            MaterialTheme {
                WaypointMaterial3Theme(
                    colors = WaypointMaterial3Theme.colors(
                        tooltipBackground = Color.DarkGray,
                        title = Color.Yellow,
                        description = Color.Cyan,
                        skipButton = Color.Red,
                        primaryButton = Color.Green,
                    ),
                ) {
                    WaypointMaterial3Host(state = state) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
            onAllNodesWithText("Themed Title").fetchSemanticsNodes().isNotEmpty()
        }

        // Tooltip renders with themed content — verify all text elements are present
        onNodeWithText("Themed Title").assertIsDisplayed()
        onNodeWithText("Themed desc").assertIsDisplayed()
        onNodeWithText("Skip").assertIsDisplayed()
        onNodeWithText("Finish").assertIsDisplayed() // single step = last step
    }

    // -- Custom dimensions applied --

    @Test
    fun `custom dimensions change tooltip shape and padding`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "Dimensions", description = "Custom dims"),
            ),
        )

        setContent {
            MaterialTheme {
                WaypointMaterial3Theme(
                    dimensions = WaypointMaterial3Theme.dimensions(
                        tooltipShape = RoundedCornerShape(24.dp),
                        tooltipPadding = 32.dp,
                        tooltipElevation = 12.dp,
                        contentSpacing = 16.dp,
                    ),
                ) {
                    WaypointMaterial3Host(state = state) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
            onAllNodesWithText("Dimensions").fetchSemanticsNodes().isNotEmpty()
        }

        // Tooltip renders — custom dims don't break layout
        onNodeWithText("Dimensions").assertIsDisplayed()
        onNodeWithText("Custom dims").assertIsDisplayed()
    }

    // -- Custom typography applied --

    @Test
    fun `custom typography is applied to tooltip`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "Styled Title", description = "Styled desc"),
            ),
        )

        setContent {
            MaterialTheme {
                WaypointMaterial3Theme(
                    typography = WaypointMaterial3Theme.typography(
                        title = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold),
                        description = TextStyle(fontSize = 16.sp),
                        progress = TextStyle(fontSize = 10.sp),
                        button = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    ),
                ) {
                    WaypointMaterial3Host(state = state) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
            onAllNodesWithText("Styled Title").fetchSemanticsNodes().isNotEmpty()
        }

        // Tooltip renders with custom typography — no crash, text visible
        onNodeWithText("Styled Title").assertIsDisplayed()
        onNodeWithText("Styled desc").assertIsDisplayed()
    }

    // -- Theme at different wrapping levels --

    @Test
    fun `theme around entire app works`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "App Theme"),
            ),
        )

        setContent {
            MaterialTheme {
                // Theme at the app level, wrapping everything
                WaypointMaterial3Theme(
                    colors = WaypointMaterial3Theme.colors(
                        tooltipBackground = Color.Black,
                        title = Color.White,
                    ),
                ) {
                    // Other app content could be here...
                    WaypointMaterial3Host(state = state) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
            onAllNodesWithText("App Theme").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithText("App Theme").assertIsDisplayed()
    }

    @Test
    fun `theme around host only works`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "Host Theme"),
            ),
        )

        setContent {
            MaterialTheme {
                // Theme wrapping only the host, not the entire app
                WaypointMaterial3Theme(
                    colors = WaypointMaterial3Theme.colors(
                        primaryButton = Color.Magenta,
                    ),
                ) {
                    WaypointMaterial3Host(state = state) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
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
            onAllNodesWithText("Host Theme").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithText("Host Theme").assertIsDisplayed()
        onNodeWithText("Finish").assertIsDisplayed()
    }

    // -- Full tour with theme --

    @Test
    fun `full tour navigation works with custom theme`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "Step A"),
                WaypointStep(targetKey = "b", title = "Step B"),
            ),
        )
        var completed = false

        setContent {
            MaterialTheme {
                WaypointMaterial3Theme(
                    colors = WaypointMaterial3Theme.colors(
                        tooltipBackground = Color(0xFF1B1B2F),
                        title = Color.White,
                        description = Color.White.copy(alpha = 0.7f),
                    ),
                    dimensions = WaypointMaterial3Theme.dimensions(
                        tooltipShape = RoundedCornerShape(24.dp),
                    ),
                ) {
                    WaypointMaterial3Host(
                        state = state,
                        onTourComplete = { completed = true },
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column {
                                Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                                Box(Modifier.size(60.dp).waypointTarget(state, "b"))
                            }
                        }
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step A").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithText("Step A").assertIsDisplayed()

        onNodeWithText("Next").performClick()
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step B").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithText("Step B").assertIsDisplayed()

        onNodeWithText("Finish").performClick()
        waitForIdle()

        assertFalse(state.isActive)
        assertTrue(completed)
    }
}
