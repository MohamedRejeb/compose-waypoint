package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * [TargetInteraction.PassThrough] scopes touches: gestures inside the
 * highlighted areas (the target and its additional targets) reach the app,
 * everything outside stays blocked and counts as an overlay click.
 *
 * Every test drives real pointer input through the root, so the hit testing
 * of the blockers is what is being exercised.
 *
 * Layout: "outside" in the top-left corner, "anchor" (the step's target) in
 * the center, "area" (an additional target) in the bottom-right corner.
 */
@OptIn(ExperimentalTestApi::class)
class PassThroughUiTest {

    /** What the app's own UI received. */
    private class Received {
        var outsideClicks = 0
        var anchorClicks = 0
        var areaClicks = 0
        var outsideDrag = Offset.Zero
        var areaDrag = Offset.Zero
        var ancestorDrag = Offset.Zero
    }

    private fun passThroughState(
        additionalTargets: List<String> = listOf("area"),
    ) = WaypointState(
        steps = listOf(
            WaypointStep(
                targetKey = "anchor",
                interaction = TargetInteraction.PassThrough,
                additionalTargets = additionalTargets,
            ),
            WaypointStep(targetKey = "area", interaction = TargetInteraction.PassThrough),
        ),
    )

    private fun runPassThroughTest(
        state: WaypointState<String> = passThroughState(),
        overlayClickBehavior: OverlayClickBehavior = OverlayClickBehavior.Nothing,
        highlightStyle: HighlightStyle = HighlightStyle.Spotlight(),
        block: ComposeUiTest.(WaypointState<String>, Received) -> Unit,
    ) = runComposeUiTest {
        val received = Received()

        setContent {
            // An ancestor that reacts to drags, like a pager or a scroll
            // container around the host would.
            Box(
                Modifier.fillMaxSize().pointerInput(Unit) {
                    detectDragGestures { _, drag -> received.ancestorDrag += drag }
                },
            ) {
                WaypointHost(
                    state = state,
                    overlayClickBehavior = overlayClickBehavior,
                    highlightStyle = highlightStyle,
                    tooltipContent = { _ -> BasicText("Tooltip") },
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Box(
                            Modifier.align(Alignment.TopStart)
                                .size(100.dp)
                                .testTag("outside")
                                .clickable { received.outsideClicks++ }
                                .pointerInput(Unit) {
                                    detectDragGestures { _, drag -> received.outsideDrag += drag }
                                },
                        )
                        Box(
                            Modifier.align(Alignment.Center)
                                .size(60.dp)
                                .testTag("anchor")
                                .clickable { received.anchorClicks++ }
                                .waypointTarget(state, "anchor"),
                        )
                        Box(
                            Modifier.align(Alignment.BottomEnd)
                                .size(160.dp)
                                .testTag("area")
                                .clickable { received.areaClicks++ }
                                .pointerInput(Unit) {
                                    detectDragGestures { _, drag -> received.areaDrag += drag }
                                }
                                .waypointTarget(state, "area"),
                        )
                    }
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
    fun `tap inside the target reaches the app`() = runPassThroughTest { state, received ->
        onNodeWithTag("anchor").performClick()
        waitForIdle()

        assertEquals(1, received.anchorClicks)
        assertTrue(state.isActive)
        assertEquals(0, state.currentStepIndex, "a pass-through tap must not move the tour")
    }

    @Test
    fun `tap inside an additional target reaches the app`() = runPassThroughTest { state, received ->
        onNodeWithTag("area").performClick()
        waitForIdle()

        assertEquals(1, received.areaClicks)
        assertEquals(0, state.currentStepIndex)
    }

    @Test
    fun `tap outside the highlighted areas is blocked`() = runPassThroughTest { state, received ->
        onNodeWithTag("outside").performClick()
        waitForIdle()

        assertEquals(0, received.outsideClicks, "the app must not see taps outside the holes")
        assertTrue(state.isActive)
        assertEquals(0, state.currentStepIndex)
    }

    @Test
    fun `tap outside triggers the overlay click behavior`() = run {
        var overlayClicks = 0

        runPassThroughTest(
            overlayClickBehavior = OverlayClickBehavior.Custom { overlayClicks++ },
        ) { _, received ->
            onNodeWithTag("outside").performClick()
            waitForIdle()

            assertEquals(1, overlayClicks)
            assertEquals(0, received.outsideClicks)

            // Taps that pass through are not overlay clicks.
            onNodeWithTag("anchor").performClick()
            onNodeWithTag("area").performClick()
            waitForIdle()

            assertEquals(1, overlayClicks)
        }
    }

    @Test
    fun `tap outside with Dismiss cancels the tour`() = runPassThroughTest(
        overlayClickBehavior = OverlayClickBehavior.Dismiss,
    ) { state, received ->
        onNodeWithTag("outside").performClick()
        waitForIdle()

        assertFalse(state.isActive)
        assertEquals(0, received.outsideClicks)
    }

    @Test
    fun `an area that is not an additional target stays blocked`() = runPassThroughTest(
        state = passThroughState(additionalTargets = emptyList()),
    ) { _, received ->
        onNodeWithTag("area").performClick()
        onNodeWithTag("anchor").performClick()
        waitForIdle()

        assertEquals(0, received.areaClicks)
        assertEquals(1, received.anchorClicks)
    }

    @Test
    fun `drag inside a highlighted area reaches the app`() = runPassThroughTest { _, received ->
        onNodeWithTag("area").performTouchInput {
            swipe(start = center, end = center + Offset(-40f, -40f), durationMillis = 100)
        }
        waitForIdle()

        assertTrue(
            received.areaDrag.getDistance() > 0f,
            "drag inside the hole did not reach the app: ${received.areaDrag}",
        )
    }

    @Test
    fun `drag outside the highlighted areas reaches neither the app nor ancestors`() =
        runPassThroughTest { _, received ->
            onNodeWithTag("outside").performTouchInput {
                swipe(start = center, end = center + Offset(40f, 40f), durationMillis = 100)
            }
            waitForIdle()

            assertEquals(Offset.Zero, received.outsideDrag)
            assertEquals(Offset.Zero, received.ancestorDrag)
        }

    @Test
    fun `invisible scrim still blocks outside and passes inside`() = runPassThroughTest(
        highlightStyle = HighlightStyle.Spotlight(overlayAlpha = 0f),
    ) { _, received ->
        onNodeWithTag("outside").performClick()
        onNodeWithTag("anchor").performClick()
        waitForIdle()

        assertEquals(0, received.outsideClicks)
        assertEquals(1, received.anchorClicks)
    }

    @Test
    fun `holes follow the tour to the next step`() = runPassThroughTest { state, received ->
        runOnIdle { state.next() }
        waitForIdle()
        assertEquals(1, state.currentStepIndex)

        // Step 2 only opens "area": the previous target is blocked again.
        onNodeWithTag("anchor").performClick()
        onNodeWithTag("area").performClick()
        waitForIdle()

        assertEquals(0, received.anchorClicks)
        assertEquals(1, received.areaClicks)
    }

    @Test
    fun `nothing is blocked once the tour ends`() = runPassThroughTest { state, received ->
        runOnIdle { state.stop() }
        waitForIdle()

        onNodeWithTag("outside").performClick()
        waitForIdle()

        assertEquals(1, received.outsideClicks)
    }

    @Test
    fun `None interaction swallows taps on the target`() = runPassThroughTest(
        state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "anchor", interaction = TargetInteraction.None)),
        ),
    ) { _, received ->
        onNodeWithTag("anchor").performClick()
        onNodeWithTag("outside").performClick()
        waitForIdle()

        assertEquals(0, received.anchorClicks)
        assertEquals(0, received.outsideClicks)
    }

    @Test
    fun `ClickToAdvance advances without the target receiving the tap`() = runPassThroughTest(
        state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "anchor", interaction = TargetInteraction.ClickToAdvance),
                WaypointStep(targetKey = "area"),
            ),
        ),
    ) { state, received ->
        onNodeWithTag("anchor").performClick()
        waitForIdle()

        assertEquals(0, received.anchorClicks)
        assertEquals(1, state.currentStepIndex)
    }
}
