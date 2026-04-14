package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import kotlin.test.Test

/**
 * Screenshot tests for tooltip placement positions.
 *
 * Captures WaypointHost with tooltip at each placement and compares
 * against goldens. Target is positioned to give enough room for
 * each requested placement without flipping.
 */
@OptIn(ExperimentalTestApi::class)
class TooltipPlacementScreenshotTest {

    private val containerSize = 500.dp

    /** Simple styled tooltip for consistent screenshots */
    private val tooltipContent: @Composable (StepScope, ResolvedPlacement) -> Unit =
        { _, _ ->
            val shape = RoundedCornerShape(8.dp)
            Column(
                modifier = Modifier
                    .shadow(4.dp, shape)
                    .clip(shape)
                    .background(Color.White)
                    .padding(12.dp),
            ) {
                BasicText("Tooltip Title")
                BasicText("Description text here")
            }
        }

    private fun runPlacementTest(
        name: String,
        placement: TooltipPlacement,
        targetAlignment: Alignment,
    ) = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    placement = placement,
                ),
            ),
        )

        setContent {
            Box(
                modifier = Modifier
                    .size(containerSize)
                    .background(Color(0xFFF5F5F5))
                    .testTag("screenshot"),
            ) {
                WaypointHost(
                    state = state,
                    tooltipContent = tooltipContent,
                ) {
                    Box(
                        modifier = Modifier.size(containerSize),
                        contentAlignment = targetAlignment,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color(0xFF2196F3), RoundedCornerShape(8.dp))
                                .waypointTarget(state, "target"),
                        )
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip Title").fetchSemanticsNodes().isNotEmpty()
        }
        // Let animations settle
        waitForIdle()

        val image = onNodeWithTag("screenshot").captureToImage()
        ScreenshotTestHelper.assertMatchesGolden(name, image)
    }

    @Test
    fun `tooltip placement - bottom`() = runPlacementTest(
        name = "placement-bottom",
        placement = TooltipPlacement.Bottom,
        // Target near top-center so tooltip has room below
        targetAlignment = Alignment.TopCenter,
    )

    @Test
    fun `tooltip placement - top`() = runPlacementTest(
        name = "placement-top",
        placement = TooltipPlacement.Top,
        // Target near bottom-center so tooltip has room above
        targetAlignment = Alignment.BottomCenter,
    )

    @Test
    fun `tooltip placement - end`() = runPlacementTest(
        name = "placement-end",
        placement = TooltipPlacement.End,
        // Target on left so tooltip has room to the right
        targetAlignment = Alignment.CenterStart,
    )

    @Test
    fun `tooltip placement - start`() = runPlacementTest(
        name = "placement-start",
        placement = TooltipPlacement.Start,
        // Target on right so tooltip has room to the left
        targetAlignment = Alignment.CenterEnd,
    )

    @Test
    fun `tooltip placement - auto`() = runPlacementTest(
        name = "placement-auto",
        placement = TooltipPlacement.Auto,
        // Target centered — auto should pick whichever side has most space
        targetAlignment = Alignment.Center,
    )
}
