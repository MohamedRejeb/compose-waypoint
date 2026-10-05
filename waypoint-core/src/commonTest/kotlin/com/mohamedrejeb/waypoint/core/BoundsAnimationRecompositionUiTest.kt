package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The highlight's bounds animation runs in the draw and layout phases: while
 * the highlight glides to a target that moved, nothing recomposes.
 */
@OptIn(ExperimentalTestApi::class)
class BoundsAnimationRecompositionUiTest {

    /** How many times any recomposer has composed and applied changes. */
    private fun changeCount(): Long =
        Recomposer.runningRecomposers.value.sumOf { it.changeCount }

    private fun runMovingTargetTest(
        style: HighlightStyle,
        interaction: TargetInteraction = TargetInteraction.None,
    ) = runComposeUiTest {
        var x by mutableStateOf(20.dp)
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "target", interaction = interaction)),
        )
        setContent {
            WaypointHost(
                state = state,
                highlightStyle = style,
                tooltipContent = { _ -> BasicText("Tooltip") },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.offset(x = x, y = 200.dp).size(60.dp).waypointTarget(state, "target"))
                }
            }
        }
        runOnIdle { state.start() }
        awaitTooltip()

        // Move the target: the highlight animates to it over 400ms.
        mainClock.autoAdvance = false
        runOnIdle { x = 400.dp }
        // Let the move itself (new bounds, tooltip repositioning) compose.
        mainClock.advanceTimeBy(100)
        val afterMove = changeCount()

        // The middle of the glide is pure animation.
        mainClock.advanceTimeBy(200)
        val midGlide = changeCount()

        mainClock.autoAdvance = true
        waitForIdle()

        assertEquals(afterMove, midGlide, "the bounds animation recomposed on its frames")
    }

    private fun ComposeUiTest.awaitTooltip() {
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()
    }

    @Test
    fun `spotlight glide does not recompose`() = runMovingTargetTest(HighlightStyle.Spotlight())

    @Test
    fun `pass-through spotlight glide does not recompose`() =
        runMovingTargetTest(HighlightStyle.Spotlight(), TargetInteraction.PassThrough)

    @Test
    fun `border glide does not recompose`() = runMovingTargetTest(HighlightStyle.Border(color = Color.Red))
}
