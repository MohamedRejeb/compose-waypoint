package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Touch blocking is independent of the highlight style: `blockOutside` on the
 * host (overridable per step) decides whether anything outside the
 * highlighted areas is blocked, for every style including [HighlightStyle.None].
 */
@OptIn(ExperimentalTestApi::class)
class BlockOutsideUiTest {

    private class Received {
        var outsideClicks = 0
        var targetClicks = 0
    }

    private fun runBlockTest(
        step: WaypointStep<String>,
        hostBlockOutside: Boolean = true,
        highlightStyle: HighlightStyle = HighlightStyle.None,
        overlayClickBehavior: OverlayClickBehavior = OverlayClickBehavior.Nothing,
        block: ComposeUiTest.(WaypointState<String>, Received) -> Unit,
    ) = runComposeUiTest {
        val received = Received()
        val state = WaypointState(steps = listOf(step, WaypointStep(targetKey = "target")))
        setContent {
            WaypointHost(
                state = state,
                highlightStyle = highlightStyle,
                blockOutside = hostBlockOutside,
                overlayClickBehavior = overlayClickBehavior,
                tooltipContent = { _ -> BasicText("Tooltip") },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(
                        Modifier.align(Alignment.TopStart).size(100.dp).testTag("outside")
                            .clickable { received.outsideClicks++ },
                    )
                    Box(
                        Modifier.align(Alignment.Center).size(60.dp).testTag("target")
                            .clickable { received.targetClicks++ }
                            .waypointTarget(state, "target"),
                    )
                }
            }
        }
        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()
        block(state, received)
    }

    @Test
    fun `None style blocks outside by default`() = runBlockTest(
        WaypointStep(targetKey = "target"),
    ) { state, received ->
        onNodeWithTag("outside").performClick()
        onNodeWithTag("target").performClick()
        waitForIdle()

        assertEquals(0, received.outsideClicks, "outside must be blocked with HighlightStyle.None")
        assertEquals(0, received.targetClicks, "None interaction swallows the target tap")
        assertTrue(state.isActive)
    }

    @Test
    fun `None style with PassThrough lets the target through and blocks outside`() = runBlockTest(
        WaypointStep(targetKey = "target", interaction = TargetInteraction.PassThrough),
    ) { _, received ->
        onNodeWithTag("outside").performClick()
        onNodeWithTag("target").performClick()
        waitForIdle()

        assertEquals(0, received.outsideClicks)
        assertEquals(1, received.targetClicks)
    }

    @Test
    fun `None style with ClickToAdvance advances on a target tap`() = runBlockTest(
        WaypointStep(targetKey = "target", interaction = TargetInteraction.ClickToAdvance),
    ) { state, received ->
        onNodeWithTag("target").performClick()
        waitForIdle()

        assertEquals(1, state.currentStepIndex)
        assertEquals(0, received.targetClicks)
    }

    @Test
    fun `outside tap with blocking applies the overlay click behavior for any style`() = runBlockTest(
        WaypointStep(targetKey = "target"),
        highlightStyle = HighlightStyle.Border(color = Color.Red),
        overlayClickBehavior = OverlayClickBehavior.Dismiss,
    ) { state, received ->
        onNodeWithTag("outside").performClick()
        waitForIdle()

        assertFalse(state.isActive)
        assertEquals(0, received.outsideClicks)
    }

    @Test
    fun `Spotlight with blockOutside false lets every tap through`() = runBlockTest(
        WaypointStep(targetKey = "target"),
        hostBlockOutside = false,
        highlightStyle = HighlightStyle.Spotlight(),
    ) { state, received ->
        onNodeWithTag("outside").performClick()
        onNodeWithTag("target").performClick()
        waitForIdle()

        assertEquals(1, received.outsideClicks)
        assertEquals(1, received.targetClicks)
        assertTrue(state.isActive)
        assertEquals(0, state.currentStepIndex)
    }

    @Test
    fun `step override wins over the host`() {
        runBlockTest(
            WaypointStep(targetKey = "target", blockOutside = true),
            hostBlockOutside = false,
            highlightStyle = HighlightStyle.Pulse(color = Color.Red),
        ) { _, received ->
            onNodeWithTag("outside").performClick()
            waitForIdle()
            assertEquals(0, received.outsideClicks, "the step asked for blocking")
        }
        runBlockTest(
            WaypointStep(targetKey = "target", blockOutside = false),
            hostBlockOutside = true,
            highlightStyle = HighlightStyle.Spotlight(),
        ) { _, received ->
            onNodeWithTag("outside").performClick()
            waitForIdle()
            assertEquals(1, received.outsideClicks, "the step asked for no blocking")
        }
    }

    @Test
    fun `target-less step blocks with any style and not when blocking is off`() {
        runBlockTest(
            WaypointStep(title = "Intro"),
            highlightStyle = HighlightStyle.None,
        ) { _, received ->
            onNodeWithTag("outside").performClick()
            waitForIdle()
            assertEquals(0, received.outsideClicks)
        }
        runBlockTest(
            WaypointStep(title = "Intro"),
            hostBlockOutside = false,
            highlightStyle = HighlightStyle.Spotlight(),
        ) { _, received ->
            onNodeWithTag("outside").performClick()
            waitForIdle()
            assertEquals(1, received.outsideClicks)
        }
    }

    @Test
    fun `pending cover blocks only when blocking resolves to true`() {
        var clicks = 0
        runComposeUiTest {
            val state = WaypointState(steps = listOf(WaypointStep(targetKey = "late")))
            setContent {
                WaypointHost(
                    state = state,
                    highlightStyle = HighlightStyle.Spotlight(coverWhilePending = true),
                    blockOutside = false,
                    tooltipContent = { _ -> BasicText("Tooltip") },
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Box(Modifier.size(100.dp).testTag("app").clickable { clicks++ })
                    }
                }
            }
            runOnIdle { state.start() }
            waitForIdle()

            onNodeWithTag("app").performClick()
            waitForIdle()

            assertEquals(1, clicks, "the cover must not block when blockOutside is false")
        }
    }
}
