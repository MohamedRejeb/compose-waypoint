package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertTrue

/** The tooltip never grows past the screen margin, whatever its content asks for. */
@OptIn(ExperimentalTestApi::class)
class TooltipPopupSizeUiTest {

    @Test
    fun `tooltip width is limited to the window minus the screen margin`() = runComposeUiTest {
        val margin = 16.dp
        val state = WaypointState(steps = listOf(WaypointStep(targetKey = "t")))
        setContent {
            WaypointHost(
                state = state,
                modifier = Modifier.testTag("host"),
                screenMargin = margin,
                tooltipContent = { _ -> Box(Modifier.width(3000.dp).height(40.dp).testTag("tip")) },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "t"))
                }
            }
        }
        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) { onAllNodesWithTag("tip").fetchSemanticsNodes().isNotEmpty() }
        waitForIdle()

        // The host fills the window.
        val window = onNodeWithTag("host").getBoundsInRoot()
        val tip = onNodeWithTag("tip").getBoundsInRoot()
        val tipWidth = (tip.right - tip.left).value
        assertTrue(
            tipWidth <= (window.right - window.left).value - margin.value * 2 + 1f,
            "tooltip $tipWidth wider than the window ${window.right - window.left} minus margins",
        )
        assertTrue(
            tip.left.value >= margin.value - 1f && tip.right.value <= window.right.value - margin.value + 1f,
            "tooltip $tip runs past the margin",
        )
    }

    @Test
    fun `tooltip height is limited to the window minus the screen margin`() = runComposeUiTest {
        val margin = 16.dp
        val state = WaypointState(steps = listOf(WaypointStep(targetKey = "t")))
        setContent {
            WaypointHost(
                state = state,
                modifier = Modifier.testTag("host"),
                screenMargin = margin,
                tooltipContent = { _ -> Box(Modifier.width(40.dp).height(5000.dp).testTag("tip")) },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "t"))
                }
            }
        }
        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) { onAllNodesWithTag("tip").fetchSemanticsNodes().isNotEmpty() }
        waitForIdle()

        val window = onNodeWithTag("host").getBoundsInRoot()
        val tip = onNodeWithTag("tip").getBoundsInRoot()
        val tipHeight = (tip.bottom - tip.top).value
        assertTrue(
            tipHeight <= (window.bottom - window.top).value - margin.value * 2 + 1f,
            "tooltip $tipHeight taller than allowed",
        )
    }
}
