package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/** [HighlightStyle.Custom] content receives the additional targets of the step too. */
@OptIn(ExperimentalTestApi::class)
class CustomHighlightUiTest {

    @Test
    fun `custom content receives the bounds of the additional targets`() = runComposeUiTest {
        var received: List<Rect>? = null
        var primary: Rect? = null
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "a", additionalTargets = listOf("b", "missing"))),
        )
        setContent {
            WaypointHost(
                state = state,
                highlightStyle = HighlightStyle.Custom { target, _, additional ->
                    primary = target
                    received = additional
                },
                tooltipContent = { _ -> BasicText("Tooltip") },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.align(Alignment.TopStart).size(60.dp).waypointTarget(state, "a"))
                    Box(Modifier.align(Alignment.BottomEnd).size(80.dp).waypointTarget(state, "b"))
                }
            }
        }
        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()

        assertEquals(state.targetCoordinates["a"], primary)
        assertEquals(listOfNotNull(state.targetCoordinates["b"]), received, "unregistered additional targets are left out")
    }
}
