package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * [TooltipArrowBox] adds an arrow on the edge of its content that faces the
 * target, and nothing when there is no target to point at.
 *
 * The arrow is detected through layout: the box grows by the arrow size on
 * the side where the arrow sits, and matches its content everywhere else.
 */
@OptIn(ExperimentalTestApi::class)
class TooltipArrowBoxUiTest {

    private val arrowSize = 12.dp

    private class Bounds(val box: DpRect, val card: DpRect) {
        val above get() = (card.top - box.top).value
        val below get() = (box.bottom - card.bottom).value
        val before get() = (card.left - box.left).value
        val after get() = (box.right - card.right).value
    }

    private fun ComposeUiTest.bounds() = Bounds(
        box = onNodeWithTag("arrow-box").getBoundsInRoot(),
        card = onNodeWithTag("card").getBoundsInRoot(),
    )

    private fun assertClose(expected: Float, actual: Float, what: String) {
        assertTrue(abs(expected - actual) <= 1f, "$what: expected $expected, was $actual")
    }

    @Composable
    private fun ArrowBoxContent() {
        TooltipArrowBox(
            arrowColor = Color.Red,
            modifier = Modifier.testTag("arrow-box"),
            arrowSize = arrowSize,
        ) {
            Box(Modifier.size(120.dp, 40.dp).testTag("card"))
        }
    }

    private fun runTourTest(
        step: WaypointStep<String>,
        block: ComposeUiTest.() -> Unit,
    ) = runComposeUiTest {
        val state = WaypointState(steps = listOf(step))
        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _ -> ArrowBoxContent() },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "target"))
                }
            }
        }
        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithTag("card").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()
        block()
    }

    @Test
    fun `tooltip below the target has the arrow on its top edge`() = runTourTest(
        WaypointStep(targetKey = "target", placement = TooltipPlacement.Bottom),
    ) {
        val bounds = bounds()

        assertClose(arrowSize.value, bounds.above, "space above the card")
        assertClose(0f, bounds.below, "space below the card")
        assertClose(0f, bounds.before, "space before the card")
        assertClose(0f, bounds.after, "space after the card")
    }

    @Test
    fun `tooltip above the target has the arrow on its bottom edge`() = runTourTest(
        WaypointStep(targetKey = "target", placement = TooltipPlacement.Top),
    ) {
        val bounds = bounds()

        assertClose(0f, bounds.above, "space above the card")
        assertClose(arrowSize.value, bounds.below, "space below the card")
        assertClose(0f, bounds.before, "space before the card")
        assertClose(0f, bounds.after, "space after the card")
    }

    @Test
    fun `tooltip at the end of the target has the arrow on its start edge`() = runTourTest(
        WaypointStep(targetKey = "target", placement = TooltipPlacement.End),
    ) {
        val bounds = bounds()

        assertClose(arrowSize.value, bounds.before, "space before the card")
        assertClose(0f, bounds.after, "space after the card")
        assertClose(0f, bounds.above, "space above the card")
        assertClose(0f, bounds.below, "space below the card")
    }

    @Test
    fun `tooltip at the start of the target has the arrow on its end edge`() = runTourTest(
        WaypointStep(targetKey = "target", placement = TooltipPlacement.Start),
    ) {
        val bounds = bounds()

        assertClose(0f, bounds.before, "space before the card")
        assertClose(arrowSize.value, bounds.after, "space after the card")
        assertClose(0f, bounds.above, "space above the card")
        assertClose(0f, bounds.below, "space below the card")
    }

    @Test
    fun `step without a target gets no arrow`() = runTourTest(WaypointStep(title = "Intro")) {
        val bounds = bounds()

        assertClose(0f, bounds.above, "space above the card")
        assertClose(0f, bounds.below, "space below the card")
        assertClose(0f, bounds.before, "space before the card")
        assertClose(0f, bounds.after, "space after the card")
    }

    @Test
    fun `no arrow outside a tooltip popup`() = runComposeUiTest {
        setContent { ArrowBoxContent() }
        waitForIdle()

        val bounds = bounds()

        assertClose(0f, bounds.above, "space above the card")
        assertClose(0f, bounds.below, "space below the card")
        assertClose(0f, bounds.before, "space before the card")
        assertClose(0f, bounds.after, "space after the card")
    }
}
