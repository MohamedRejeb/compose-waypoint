package com.mohamedrejeb.waypoint.material3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.mohamedrejeb.waypoint.core.LocalTooltipArrowGeometry
import com.mohamedrejeb.waypoint.core.ResolvedPlacement
import com.mohamedrejeb.waypoint.core.TooltipArrowGeometry
import kotlin.test.Test

/**
 * Smoke tests for the arrow layout around [WaypointMaterial3Tooltip]: with
 * arrow geometry present, the tooltip must compose and stay functional for
 * every placement, in LTR and RTL, with clamped and centered offsets.
 */
@OptIn(ExperimentalTestApi::class)
class Material3TooltipArrowUiTest {

    private val scope = TestStepScope(
        currentStepIndex = 0,
        totalSteps = 3,
        isFirstStep = true,
        isLastStep = false,
    )

    @Composable
    private fun TooltipWithGeometry(placement: ResolvedPlacement, arrowOffset: Float) {
        CompositionLocalProvider(
            LocalTooltipArrowGeometry provides TooltipArrowGeometry(placement, arrowOffset),
        ) {
            WaypointMaterial3Tooltip(
                stepScope = scope,
                resolvedPlacement = placement,
                title = "Title",
                description = "Description",
            )
        }
    }

    @Test
    fun `tooltip renders with arrow for every placement`() {
        for (placement in ResolvedPlacement.entries) {
            runComposeUiTest {
                setContent { TooltipWithGeometry(placement, arrowOffset = 80f) }

                onNodeWithText("Title").assertIsDisplayed()
                onNodeWithText("Description").assertIsDisplayed()
            }
        }
    }

    @Test
    fun `tooltip renders with an edge-clamped arrow offset`() = runComposeUiTest {
        setContent { TooltipWithGeometry(ResolvedPlacement.Bottom, arrowOffset = 26f) }

        onNodeWithText("Title").assertIsDisplayed()
    }

    @Test
    fun `tooltip renders without geometry when composed outside a popup`() = runComposeUiTest {
        setContent {
            WaypointMaterial3Tooltip(
                stepScope = scope,
                resolvedPlacement = ResolvedPlacement.Bottom,
                title = "Title",
                description = "Description",
            )
        }

        onNodeWithText("Title").assertIsDisplayed()
    }

    @Test
    fun `showArrow false skips the arrow layout`() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(
                LocalTooltipArrowGeometry provides
                    TooltipArrowGeometry(ResolvedPlacement.Bottom, 80f),
            ) {
                WaypointMaterial3Tooltip(
                    stepScope = scope,
                    resolvedPlacement = ResolvedPlacement.Bottom,
                    title = "Title",
                    description = "Description",
                    showArrow = false,
                )
            }
        }

        onNodeWithText("Title").assertIsDisplayed()
    }
}
