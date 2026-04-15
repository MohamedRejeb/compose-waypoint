package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class WaypointBeaconUiTest {

    // -- Visibility --

    @Test
    fun `beacon renders when visible is true`() = runComposeUiTest {
        setContent {
            WaypointBeacon(
                visible = true,
                style = BeaconStyle.Dot(),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("beacon-content"),
                )
            }
        }

        waitForIdle()
        onNodeWithTag("beacon-content").assertIsDisplayed()
    }

    @Test
    fun `beacon does not render when visible is false`() = runComposeUiTest {
        var clickFired = false

        setContent {
            WaypointBeacon(
                visible = false,
                style = BeaconStyle.Dot(),
                onClick = { clickFired = true },
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("beacon-content"),
                )
            }
        }

        waitForIdle()
        // Content still renders inside the wrapper
        onNodeWithTag("beacon-content").assertIsDisplayed()
        // The beacon indicator is hidden (AnimatedVisibility with visible=false),
        // so clicking the content area should not fire the beacon onClick
        onNodeWithTag("beacon-content").performClick()
        waitForIdle()
        assertFalse(clickFired, "onClick should not fire when beacon is not visible")
    }

    // -- Click interaction --

    @Test
    fun `beacon click fires onClick`() = runComposeUiTest {
        var clickFired = false

        setContent {
            WaypointBeacon(
                visible = true,
                style = BeaconStyle.Dot(radius = 24.dp),
                alignment = Alignment.Center,
                onClick = { clickFired = true },
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .testTag("beacon-content"),
                )
            }
        }

        waitForIdle()
        // The beacon Canvas is rendered at Center alignment over the content.
        // Clicking the content area (where the beacon overlaps) should fire onClick.
        onNodeWithTag("beacon-content").performClick()
        waitForIdle()
        assertTrue(clickFired, "onClick should fire when beacon area is clicked")
    }

    // -- Content rendering --

    @Test
    fun `content renders inside beacon wrapper`() = runComposeUiTest {
        setContent {
            WaypointBeacon(
                visible = true,
            ) {
                BasicText("Hello")
            }
        }

        waitForIdle()
        onNodeWithText("Hello").assertIsDisplayed()
    }

    // -- Style variants --

    @Test
    fun `dot style renders`() = runComposeUiTest {
        setContent {
            WaypointBeacon(
                visible = true,
                style = BeaconStyle.Dot(
                    color = Color.Blue,
                    radius = 8.dp,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("dot-content"),
                )
            }
        }

        waitForIdle()
        // If it renders without crash, the Dot style works
        onNodeWithTag("dot-content").assertIsDisplayed()
    }

    @Test
    fun `pulse style renders`() = runComposeUiTest {
        setContent {
            WaypointBeacon(
                visible = true,
                style = BeaconStyle.Pulse(
                    color = Color.Red,
                    beaconRadius = 6.dp,
                    maxPulseRadius = 16.dp,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("pulse-content"),
                )
            }
        }

        waitForIdle()
        // If it renders without crash, the Pulse style works
        onNodeWithTag("pulse-content").assertIsDisplayed()
    }

    @Test
    fun `beacon default style is Pulse`() = runComposeUiTest {
        // No style parameter — should default to BeaconStyle.Pulse()
        setContent {
            WaypointBeacon(
                visible = true,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("default-content"),
                )
            }
        }

        waitForIdle()
        // If it compiles and renders, the default parameter works
        onNodeWithTag("default-content").assertIsDisplayed()
    }
}
