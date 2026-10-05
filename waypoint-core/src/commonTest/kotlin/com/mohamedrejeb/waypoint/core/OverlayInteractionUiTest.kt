package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tap behavior on the spotlight overlay: [OverlayClickBehavior] for taps on
 * the scrim and [TargetInteraction] for taps inside the cutout.
 *
 * The target is centered in the root, so taps near the top-left corner land
 * on the scrim and taps in the middle land inside the cutout.
 */
@OptIn(ExperimentalTestApi::class)
class OverlayInteractionUiTest {

    private fun twoStepState(interaction: TargetInteraction = TargetInteraction.None) = WaypointState(
        steps = listOf(
            WaypointStep(targetKey = "first", interaction = interaction),
            WaypointStep(targetKey = "second"),
        ),
    )

    @OptIn(ExperimentalTestApi::class)
    private fun runOverlayTest(
        state: WaypointState<String>,
        overlayClickBehavior: OverlayClickBehavior,
        onTourCancel: (() -> Unit)? = null,
        block: androidx.compose.ui.test.ComposeUiTest.() -> Unit,
    ) = runComposeUiTest {
        setContent {
            WaypointHost(
                state = state,
                overlayClickBehavior = overlayClickBehavior,
                onTourCancel = onTourCancel,
                tooltipContent = { _ -> BasicText("Tooltip") },
            ) {
                Box(
                    Modifier.fillMaxSize().testTag("screen"),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                    Box(Modifier.size(100.dp).waypointTarget(state, "second"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        block()
    }

    @Test
    fun `scrim tap with Dismiss cancels the tour and fires onTourCancel`() = run {
        val state = twoStepState()
        var cancelled = 0

        runOverlayTest(state, OverlayClickBehavior.Dismiss, onTourCancel = { cancelled++ }) {
            onNodeWithTag("screen").performTouchInput { click(Offset(10f, 10f)) }
            waitForIdle()

            assertFalse(state.isActive)
            assertEquals(1, cancelled)
        }
    }

    @Test
    fun `scrim tap with NextStep advances the tour`() = run {
        val state = twoStepState()

        runOverlayTest(state, OverlayClickBehavior.NextStep) {
            onNodeWithTag("screen").performTouchInput { click(Offset(10f, 10f)) }
            waitForIdle()

            assertTrue(state.isActive)
            assertEquals(1, state.currentStepIndex)
        }
    }

    @Test
    fun `scrim tap with Custom runs the action`() = run {
        val state = twoStepState()
        var custom = 0

        runOverlayTest(state, OverlayClickBehavior.Custom { custom++ }) {
            onNodeWithTag("screen").performTouchInput { click(Offset(10f, 10f)) }
            waitForIdle()

            assertEquals(1, custom)
            assertTrue(state.isActive)
            assertEquals(0, state.currentStepIndex)
        }
    }

    @Test
    fun `scrim tap with Nothing absorbs the click`() = run {
        val state = twoStepState()

        runOverlayTest(state, OverlayClickBehavior.Nothing) {
            onNodeWithTag("screen").performTouchInput { click(Offset(10f, 10f)) }
            waitForIdle()

            assertTrue(state.isActive)
            assertEquals(0, state.currentStepIndex)
        }
    }

    @Test
    fun `cutout tap with ClickToAdvance advances the tour`() = run {
        val state = twoStepState(interaction = TargetInteraction.ClickToAdvance)

        runOverlayTest(state, OverlayClickBehavior.Nothing) {
            onNodeWithTag("screen").performTouchInput { click(center) }
            waitForIdle()

            assertTrue(state.isActive)
            assertEquals(1, state.currentStepIndex)
        }
    }

    @Test
    fun `cutout tap with None interaction does nothing`() = run {
        val state = twoStepState(interaction = TargetInteraction.None)

        runOverlayTest(state, OverlayClickBehavior.Nothing) {
            onNodeWithTag("screen").performTouchInput { click(center) }
            waitForIdle()

            assertTrue(state.isActive)
            assertEquals(0, state.currentStepIndex)
        }
    }

    @Test
    fun `scrim tap during bounds animation still dismisses`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "left"),
                WaypointStep(targetKey = "right"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                overlayClickBehavior = OverlayClickBehavior.Dismiss,
                tooltipContent = { _ -> BasicText("Tooltip") },
            ) {
                Box(Modifier.fillMaxSize().testTag("screen")) {
                    Box(
                        Modifier.align(Alignment.CenterStart)
                            .size(80.dp)
                            .waypointTarget(state, "left"),
                    )
                    Box(
                        Modifier.align(Alignment.CenterEnd)
                            .size(80.dp)
                            .waypointTarget(state, "right"),
                    )
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Advance so the highlight animates from the left target to the right
        // target, then tap the scrim mid-animation (top-left corner is never
        // inside either cutout).
        mainClock.autoAdvance = false
        runOnIdle { state.next() }
        mainClock.advanceTimeBy(100)

        onNodeWithTag("screen").performTouchInput { click(Offset(10f, 10f)) }
        repeat(5) { mainClock.advanceTimeByFrame() }
        mainClock.autoAdvance = true
        waitForIdle()

        assertFalse(state.isActive, "tap landing mid-animation should still dismiss the tour")
    }
}
