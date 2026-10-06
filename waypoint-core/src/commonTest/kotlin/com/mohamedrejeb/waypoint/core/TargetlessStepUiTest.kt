package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Steps without a target: shown as a centered card by the primary host, with
 * a cutout-free scrim when the style is a spotlight.
 */
@OptIn(ExperimentalTestApi::class)
class TargetlessStepUiTest {

    private class Observed {
        var scope: StepScope? = null
        var arrowGeometry: TooltipArrowGeometry? = null
        var appClicks = 0
    }

    private fun intro() = WaypointStep<String>(title = "Intro")
    private fun outro() = WaypointStep<String>(title = "Outro")

    private fun runTargetlessTest(
        state: WaypointState<String>,
        overlayClickBehavior: OverlayClickBehavior = OverlayClickBehavior.Nothing,
        highlightStyle: HighlightStyle = HighlightStyle.Spotlight(),
        block: ComposeUiTest.(Observed) -> Unit,
    ) = runComposeUiTest {
        val observed = Observed()

        setContent {
            // A stray geometry from an outer scope must not leak into the card.
            CompositionLocalProvider(
                LocalTooltipArrowGeometry provides TooltipArrowGeometry(ResolvedPlacement.Top, 1f),
            ) {
                WaypointHost(
                    state = state,
                    overlayClickBehavior = overlayClickBehavior,
                    highlightStyle = highlightStyle,
                    modifier = Modifier.testTag("host"),
                    tooltipContent = { scope ->
                        observed.scope = scope
                        observed.arrowGeometry = LocalTooltipArrowGeometry.current
                        BasicText(
                            text = "tip-${scope.title}",
                            modifier = Modifier.size(120.dp, 40.dp).testTag("tooltip"),
                        )
                    },
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Box(
                            Modifier.align(Alignment.TopStart)
                                .size(100.dp)
                                .testTag("app")
                                .clickable { observed.appClicks++ },
                        )
                        Box(
                            Modifier.align(Alignment.BottomEnd)
                                .size(80.dp)
                                .waypointTarget(state, "a"),
                        )
                        Box(
                            Modifier.align(Alignment.BottomStart)
                                .size(80.dp)
                                .waypointTarget(state, "b"),
                        )
                    }
                }
            }
        }
        waitForIdle()

        block(observed)
    }

    private fun ComposeUiTest.awaitTip(title: String) {
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("tip-$title").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()
    }

    private fun ComposeUiTest.assertNoTip() {
        assertTrue(onAllNodesWithTag("tooltip").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `target-less step is shown centered over the host`() = run {
        val state = WaypointState(steps = listOf(intro()))

        runTargetlessTest(state) { observed ->
            runOnIdle { state.start() }
            awaitTip("Intro")

            val host = onNodeWithTag("host").getBoundsInRoot()
            val tip = onNodeWithTag("tooltip").getBoundsInRoot()
            val hostCenterX = (host.left + host.right) / 2
            val hostCenterY = (host.top + host.bottom) / 2
            val tipCenterX = (tip.left + tip.right) / 2
            val tipCenterY = (tip.top + tip.bottom) / 2
            assertTrue(abs((hostCenterX - tipCenterX).value) <= 1f, "x: host=$hostCenterX tip=$tipCenterX")
            assertTrue(abs((hostCenterY - tipCenterY).value) <= 1f, "y: host=$hostCenterY tip=$tipCenterY")

            val scope = observed.scope
            assertNull(scope?.placement, "a step without a target has no placement")
            assertEquals("Intro", scope?.title)
            assertNull(observed.arrowGeometry, "no arrow geometry without a target")
            assertNull(state.currentTargetBounds)
            assertTrue(state.isStepVisible)
        }
    }

    @Test
    fun `spotlight scrim blocks the app and applies the overlay click behavior`() = run {
        val state = WaypointState(steps = listOf(intro(), WaypointStep(targetKey = "a", title = "A")))

        runTargetlessTest(state, overlayClickBehavior = OverlayClickBehavior.NextStep) { observed ->
            runOnIdle { state.start() }
            awaitTip("Intro")

            onNodeWithTag("app").performClick()
            waitForIdle()

            assertEquals(0, observed.appClicks, "the scrim must block the app")
            assertEquals(1, state.currentStepIndex, "the tap counts as an overlay click")
        }
    }

    @Test
    fun `interaction and additional targets are ignored without a target`() = run {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    title = "Intro",
                    interaction = TargetInteraction.PassThrough,
                    additionalTargets = listOf("a"),
                ),
            ),
        )

        runTargetlessTest(state) { observed ->
            runOnIdle { state.start() }
            awaitTip("Intro")

            onNodeWithTag("app").performClick()
            waitForIdle()

            assertEquals(0, observed.appClicks)
            assertTrue(state.isActive)
        }
    }

    @Test
    fun `non-spotlight style draws nothing but still blocks the app by default`() = run {
        val state = WaypointState(steps = listOf(intro()))

        runTargetlessTest(state, highlightStyle = HighlightStyle.Border(color = Color.Red)) { observed ->
            runOnIdle { state.start() }
            awaitTip("Intro")

            onNodeWithTag("app").performClick()
            waitForIdle()

            assertEquals(0, observed.appClicks)
        }
    }

    @Test
    fun `next and previous move across target-less and targeted steps`() = run {
        val state = WaypointState(
            steps = listOf(
                intro(),
                WaypointStep(targetKey = "a", title = "A"),
                WaypointStep(title = "Middle"),
                WaypointStep(targetKey = "b", title = "B"),
                outro(),
            ),
        )

        runTargetlessTest(state) { observed ->
            runOnIdle { state.start() }
            awaitTip("Intro")
            assertTrue(observed.scope?.isFirstStep == true)
            assertEquals(5, observed.scope?.totalSteps)

            runOnIdle { observed.scope?.next() }
            awaitTip("A")
            assertTrue(observed.scope?.placement != null, "a targeted step has a placement")

            runOnIdle { observed.scope?.next() }
            awaitTip("Middle")
            assertNull(observed.scope?.placement)
            assertEquals(3, observed.scope?.currentStepNumber)

            runOnIdle { observed.scope?.next() }
            awaitTip("B")

            runOnIdle { observed.scope?.next() }
            awaitTip("Outro")
            assertTrue(observed.scope?.isLastStep == true)

            runOnIdle { observed.scope?.previous() }
            awaitTip("B")
            runOnIdle { observed.scope?.previous() }
            awaitTip("Middle")
            runOnIdle { observed.scope?.previous() }
            awaitTip("A")
            runOnIdle { observed.scope?.previous() }
            awaitTip("Intro")
            assertEquals(0, state.currentStepIndex)
        }
    }

    @Test
    fun `finishing on a target-less last step completes the tour`() = run {
        val state = WaypointState(steps = listOf(WaypointStep(targetKey = "a", title = "A"), outro()))

        runTargetlessTest(state) { observed ->
            runOnIdle { state.start() }
            awaitTip("A")
            runOnIdle { state.next() }
            awaitTip("Outro")

            runOnIdle { observed.scope?.next() }
            waitForIdle()

            assertFalse(state.isActive)
            assertEquals(WaypointEndReason.Completed, state.lastEndReason)
            assertNoTip()
        }
    }

    @Test
    fun `skip on a target-less step cancels the tour`() = run {
        val state = WaypointState(steps = listOf(intro(), WaypointStep(targetKey = "a", title = "A")))

        runTargetlessTest(state) { observed ->
            runOnIdle { state.start() }
            awaitTip("Intro")

            runOnIdle { observed.scope?.skip() }
            waitForIdle()

            assertEquals(WaypointEndReason.Cancelled, state.lastEndReason)
            assertNoTip()
        }
    }

    @Test
    fun `hidden target-less steps are skipped and not counted`() = run {
        var showIntro by mutableStateOf(false)
        val state = WaypointState(
            steps = listOf(
                WaypointStep(title = "Intro", showIf = { showIntro }),
                WaypointStep(targetKey = "a", title = "A"),
                WaypointStep(title = "Outro", showIf = { false }),
            ),
        )

        runTargetlessTest(state) { observed ->
            runOnIdle { state.start() }
            awaitTip("A")

            assertEquals(1, state.currentStepIndex)
            assertEquals(1, observed.scope?.totalSteps)
            assertTrue(observed.scope?.isFirstStep == true)
            assertTrue(observed.scope?.isLastStep == true)

            runOnIdle {
                state.stop()
                showIntro = true
                state.start()
            }
            awaitTip("Intro")
            assertEquals(2, observed.scope?.totalSteps)
        }
    }

    @Test
    fun `step content of a target-less step replaces the host tooltip`() = run {
        val state = WaypointState(
            steps = listOf(
                WaypointStep<String>(
                    title = "Intro",
                    content = { scope -> BasicText("custom-${scope.title}-${scope.placement}") },
                ),
            ),
        )

        runTargetlessTest(state) {
            runOnIdle { state.start() }
            waitUntil(timeoutMillis = 3000) {
                onAllNodesWithText("custom-Intro-null").fetchSemanticsNodes().isNotEmpty()
            }
            assertNoTip()
        }
    }

    @Test
    fun `lifecycle callbacks and analytics work without a target`() = run {
        val events = mutableListOf<String>()
        val analytics = object : WaypointAnalytics {
            override fun onStepViewed(tourId: String?, stepIndex: Int, targetKey: Any?) {
                events += "viewed-$stepIndex-$targetKey"
            }

            override fun onStepCompleted(tourId: String?, stepIndex: Int, targetKey: Any?) {
                events += "completed-$stepIndex-$targetKey"
            }
        }
        val state = WaypointState(
            steps = listOf(
                WaypointStep<String>(
                    title = "Intro",
                    onEnter = { events += "enter" },
                    onExit = { events += "exit" },
                ),
            ),
            analytics = analytics,
        )

        runTargetlessTest(state) {
            runOnIdle { state.start() }
            awaitTip("Intro")
            runOnIdle { state.next() }
            waitForIdle()

            assertEquals(listOf("enter", "viewed-0-null", "exit", "completed-0-null"), events)
        }
    }

    @Test
    fun `pause hides a target-less step and resume shows it again`() = run {
        val state = WaypointState(steps = listOf(intro()))

        runTargetlessTest(state) {
            runOnIdle { state.start() }
            awaitTip("Intro")

            runOnIdle { state.pause() }
            waitForIdle()
            assertNoTip()
            assertFalse(state.isStepVisible)

            runOnIdle { state.resume() }
            awaitTip("Intro")
        }
    }
}
