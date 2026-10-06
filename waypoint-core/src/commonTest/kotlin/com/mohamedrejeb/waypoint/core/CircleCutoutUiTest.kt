package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
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

/**
 * For [SpotlightShape.Circle] the drawn cutout is a circle around the target's
 * center with a radius of half its longer side, which reaches well beyond a
 * wide target's own rectangle. Tap decisions and the pass-through hole use the
 * bounding square of that circle, so what looks open is open.
 *
 * Layout: a wide 200x40dp target in the center. "inside" sits 60dp above the
 * target's center: outside the target's rectangle but inside the circle.
 * "outside" sits 200dp above it, beyond the circle.
 */
@OptIn(ExperimentalTestApi::class)
class CircleCutoutUiTest {

    private class Received {
        var insideClicks = 0
        var outsideClicks = 0
    }

    private fun runCircleTest(
        interaction: TargetInteraction,
        shape: SpotlightShape = SpotlightShape.Circle,
        block: ComposeUiTest.(WaypointState<String>, Received) -> Unit,
    ) = runComposeUiTest {
        val received = Received()
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "wide", interaction = interaction),
                WaypointStep(targetKey = "wide"),
            ),
        )
        setContent {
            WaypointHost(
                state = state,
                highlightStyle = HighlightStyle.Spotlight(shape = shape),
                tooltipContent = { _ -> BasicText("Tooltip") },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(200.dp, 40.dp).waypointTarget(state, "wide"))
                    Box(
                        Modifier.offset(y = (-60).dp)
                            .size(30.dp)
                            .testTag("inside")
                            .clickable { received.insideClicks++ },
                    )
                    Box(
                        Modifier.offset(y = (-200).dp)
                            .size(30.dp)
                            .testTag("outside")
                            .clickable { received.outsideClicks++ },
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
    fun `PassThrough opens the whole circle`() = runCircleTest(
        TargetInteraction.PassThrough,
    ) { _, received ->
        onNodeWithTag("inside").performClick()
        onNodeWithTag("outside").performClick()
        waitForIdle()

        assertEquals(1, received.insideClicks, "a tap inside the drawn circle must reach the app")
        assertEquals(0, received.outsideClicks, "a tap beyond the circle must stay blocked")
    }

    @Test
    fun `ClickToAdvance reacts to a tap anywhere in the circle`() = runCircleTest(
        TargetInteraction.ClickToAdvance,
    ) { state, received ->
        onNodeWithTag("outside").performClick()
        waitForIdle()
        assertEquals(0, state.currentStepIndex)

        onNodeWithTag("inside").performClick()
        waitForIdle()

        assertEquals(1, state.currentStepIndex)
        assertEquals(0, received.insideClicks)
    }

    @Test
    fun `rectangular cutout keeps to the padded target bounds`() = runCircleTest(
        TargetInteraction.PassThrough,
        shape = SpotlightShape.Rect,
    ) { _, received ->
        onNodeWithTag("inside").performClick()
        waitForIdle()

        assertEquals(0, received.insideClicks)
    }

    @Test
    fun `cutout bounds of a circle is the bounding square of the drawn circle`() {
        val wide = Rect(left = 100f, top = 90f, right = 300f, bottom = 110f)
        val tall = Rect(left = 190f, top = 0f, right = 210f, bottom = 200f)

        assertEquals(Rect(100f, 0f, 300f, 200f), cutoutBounds(wide, SpotlightShape.Circle))
        assertEquals(Rect(100f, 0f, 300f, 200f), cutoutBounds(tall, SpotlightShape.Circle))
    }

    @Test
    fun `cutout bounds of every other shape is the bounds themselves`() {
        val bounds = Rect(left = 100f, top = 90f, right = 300f, bottom = 110f)

        assertEquals(bounds, cutoutBounds(bounds, SpotlightShape.Rect))
        assertEquals(bounds, cutoutBounds(bounds, SpotlightShape.Pill))
        assertEquals(bounds, cutoutBounds(bounds, SpotlightShape.RoundedRect(8.dp)))
    }
}
