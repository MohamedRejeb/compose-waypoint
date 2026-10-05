package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * The highlight glides only when the step changes. When the target of the
 * current step moves (scroll, keyboard, layout change) the highlight snaps
 * to it, so the pass-through hole never lags behind the target.
 */
@OptIn(ExperimentalTestApi::class)
class BoundsSnapWithinStepUiTest {

    @Test
    fun `hole follows a target that moves within the step without a glide`() = runComposeUiTest {
        var x by mutableStateOf(20.dp)
        var clicks = 0
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "t", interaction = TargetInteraction.PassThrough)),
        )
        setContent {
            WaypointHost(state = state, tooltipContent = { _ -> BasicText("Tooltip") }) {
                Box(Modifier.fillMaxSize()) {
                    Box(
                        Modifier.offset(x = x, y = 200.dp)
                            .size(60.dp)
                            .testTag("target")
                            .clickable { clicks++ }
                            .waypointTarget(state, "t"),
                    )
                }
            }
        }
        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()

        mainClock.autoAdvance = false
        runOnIdle { x = 400.dp }
        repeat(2) { mainClock.advanceTimeByFrame() }

        // Two frames after the move the hole is already at the new position.
        onNodeWithTag("target").performClick()
        repeat(2) { mainClock.advanceTimeByFrame() }
        mainClock.autoAdvance = true
        waitForIdle()

        assertEquals(1, clicks, "the hole lagged behind the moved target")
    }

    @Test
    fun `step change still glides`() = runComposeUiTest {
        var lastAnimated: Rect? = null
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "a"), WaypointStep(targetKey = "b")),
        )
        setContent {
            WaypointHost(
                state = state,
                highlightStyle = HighlightStyle.Custom { _, animated, _ -> lastAnimated = animated },
                tooltipContent = { _ -> BasicText("Tooltip") },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.offset(x = 20.dp, y = 20.dp).size(60.dp).waypointTarget(state, "a"))
                    Box(Modifier.offset(x = 400.dp, y = 400.dp).size(60.dp).waypointTarget(state, "b"))
                }
            }
        }
        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()

        mainClock.autoAdvance = false
        runOnIdle { state.next() }
        mainClock.advanceTimeBy(100)
        val midway = lastAnimated
        mainClock.autoAdvance = true
        waitForIdle()

        assertNotEquals(state.currentTargetBounds, midway, "the step change did not animate")
    }
}
