package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * [HighlightStyle.Spotlight.coverWhilePending]: while a step is pending (its
 * gate is running, or its target is not laid out yet) the responsible host
 * draws the scrim with no cutout and blocks all input, so the app is not
 * usable between steps. Off by default.
 */
@OptIn(ExperimentalTestApi::class)
class CoverWhilePendingUiTest {

    private class App {
        var clicks = 0
        var showLate by mutableStateOf(false)
        var showDialog by mutableStateOf(false)
    }

    private fun ComposeUiTest.setTour(
        state: WaypointState<String>,
        style: HighlightStyle = HighlightStyle.Spotlight(coverWhilePending = true),
        overlayClickBehavior: OverlayClickBehavior = OverlayClickBehavior.Nothing,
    ): App {
        val app = App()
        setContent {
            WaypointHost(
                state = state,
                highlightStyle = style,
                overlayClickBehavior = overlayClickBehavior,
                tooltipContent = { scope -> BasicText("tip-${scope.title}") },
            ) {
                Box(Modifier.size(400.dp)) {
                    Box(Modifier.size(100.dp).testTag("app").clickable { app.clicks++ })
                    Box(Modifier.align(androidx.compose.ui.Alignment.Center).size(60.dp).waypointTarget(state, "a"))
                    if (app.showLate) {
                        Box(
                            Modifier.align(androidx.compose.ui.Alignment.BottomEnd)
                                .size(60.dp)
                                .waypointTarget(state, "late"),
                        )
                    }
                    if (app.showDialog) {
                        Dialog(onDismissRequest = {}) {
                            WaypointOverlayHost(
                                state = state,
                                highlightStyle = style,
                                tooltipContent = { scope -> BasicText("tip-${scope.title}") },
                            ) {
                                Box(Modifier.size(200.dp)) {
                                    Box(Modifier.size(60.dp).waypointTarget(state, "inDialog"))
                                }
                            }
                        }
                    }
                }
            }
        }
        waitForIdle()
        return app
    }

    private fun ComposeUiTest.awaitTip(title: String) {
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("tip-$title").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()
    }

    @Test
    fun `app is blocked while a gate holds the step`() = runComposeUiTest {
        val gate = CompletableDeferred<Unit>()
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "A"),
                WaypointStep(targetKey = "late", title = "Late", beforeShow = { gate.await() }),
            ),
        )
        val app = setTour(state)

        runOnIdle { state.start() }
        awaitTip("A")
        runOnIdle { state.next() }
        waitForIdle()
        assertFalse(state.isStepVisible)

        onNodeWithTag("app").performClick()
        waitForIdle()
        assertEquals(0, app.clicks, "the app was tappable while the step was pending")

        runOnIdle {
            gate.complete(Unit)
            app.showLate = true
        }
        awaitTip("Late")
    }

    @Test
    fun `app is blocked while the target is not laid out yet`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "late", title = "Late")),
        )
        val app = setTour(state)

        runOnIdle { state.start() }
        waitForIdle()

        onNodeWithTag("app").performClick()
        waitForIdle()
        assertEquals(0, app.clicks)

        runOnIdle { app.showLate = true }
        awaitTip("Late")
    }

    @Test
    fun `overlay click behavior applies to the cover`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "late", title = "Late")),
        )
        val app = setTour(state, overlayClickBehavior = OverlayClickBehavior.Dismiss)

        runOnIdle { state.start() }
        waitForIdle()

        onNodeWithTag("app").performClick()
        waitForIdle()

        assertFalse(state.isActive)
        assertEquals(0, app.clicks)
    }

    @Test
    fun `default style leaves the app usable while pending`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "late", title = "Late")),
        )
        val app = setTour(state, style = HighlightStyle.Spotlight())

        runOnIdle { state.start() }
        waitForIdle()

        onNodeWithTag("app").performClick()
        waitForIdle()
        assertEquals(1, app.clicks)
    }

    @Test
    fun `nothing is covered while paused or inactive`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "late", title = "Late")),
        )
        val app = setTour(state)

        onNodeWithTag("app").performClick()
        waitForIdle()
        assertEquals(1, app.clicks)

        runOnIdle { state.start() }
        runOnIdle { state.pause() }
        waitForIdle()
        onNodeWithTag("app").performClick()
        waitForIdle()
        assertEquals(2, app.clicks)
    }

    @Test
    fun `a target scrolled away after being shown does not trigger the cover`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "item", title = "Item", interaction = TargetInteraction.PassThrough),
            ),
        )
        var clicks = 0
        setContent {
            WaypointHost(
                state = state,
                highlightStyle = HighlightStyle.Spotlight(coverWhilePending = true),
                tooltipContent = { scope -> BasicText("tip-${scope.title}") },
            ) {
                Column(Modifier.size(300.dp)) {
                    Box(Modifier.size(60.dp).testTag("app").clickable { clicks++ })
                    Column(
                        Modifier.fillMaxWidth()
                            .height(200.dp)
                            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    ) {
                        Box(Modifier.size(60.dp).testTag("item").waypointTarget(state, "item"))
                        Spacer(Modifier.height(1000.dp))
                    }
                }
            }
        }
        runOnIdle { state.start() }
        awaitTip("Item")

        // The user scrolls the target out of the viewport from inside it.
        onNodeWithTag("item").performTouchInput { swipeUp(startY = bottom - 1f, endY = top - 300f) }
        waitForIdle()
        assertEquals(null, state.currentTargetBounds, "the target should be out of view")

        // Outside is still the normal blocked scrim, but not a trap: the step
        // stays in the hands of the user and nothing new is drawn. The app
        // button above the list was blocked before and stays blocked, the
        // point is that the host does not switch to the pending cover (which
        // would also hide the tooltip).
        assertTrue(state.isActive)
        assertTrue(
            onAllNodesWithText("tip-Item").fetchSemanticsNodes().isNotEmpty(),
            "the tooltip must stay while the target is scrolled away",
        )
    }

    @Test
    fun `the overlay host that owns a held target draws the cover`() = runComposeUiTest {
        val gate = CompletableDeferred<Unit>()
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "A"),
                WaypointStep(targetKey = "inDialog", title = "Dialog", beforeShow = { gate.await() }),
            ),
        )
        val app = setTour(state)

        runOnIdle { state.start() }
        awaitTip("A")
        runOnIdle {
            app.showDialog = true
            state.next()
        }
        waitForIdle()
        assertTrue(state.isStepHeld)
        // Ownership is resolved to the dialog's host, which draws the cover.
        assertTrue(state.currentTargetBounds != null)

        runOnIdle { gate.complete(Unit) }
        awaitTip("Dialog")
    }
}
