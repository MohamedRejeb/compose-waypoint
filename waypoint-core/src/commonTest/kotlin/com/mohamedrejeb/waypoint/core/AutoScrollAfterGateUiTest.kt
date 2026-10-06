package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Auto-scroll happens when a step becomes showable, not only at step entry:
 * a target that is mounted by beforeShow, or that registers late, is still
 * brought into view. It happens once per step, so it never fights the user.
 *
 * Layout: a 300dp tall scroll container with "top" at the start and "bottom"
 * 1800dp further down, far outside the viewport.
 */
@OptIn(ExperimentalTestApi::class)
class AutoScrollAfterGateUiTest {

    private fun ComposeUiTest.setScrollContent(
        state: WaypointState<String>,
        scrollState: ScrollState,
        showBottom: MutableState<Boolean>,
    ) {
        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { scope -> BasicText("tip-${scope.title}") },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(scrollState),
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "top"))
                    Spacer(Modifier.height(1800.dp))
                    if (showBottom.value) {
                        Box(
                            Modifier.size(100.dp)
                                .waypointTarget(state, "bottom")
                                .testTag("bottom"),
                        )
                    }
                    Spacer(Modifier.height(400.dp))
                }
            }
        }
        waitForIdle()
    }

    private fun ComposeUiTest.awaitTip(title: String) {
        waitUntil(timeoutMillis = 5000) {
            onAllNodesWithText("tip-$title").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()
    }

    @Test
    fun `target mounted by beforeShow is scrolled into view`() = runComposeUiTest {
        val showBottom = mutableStateOf(false)
        val scrollState = ScrollState(0)
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "top", title = "Top"),
                WaypointStep(
                    targetKey = "bottom",
                    title = "Bottom",
                    beforeShow = { showBottom.value = true },
                ),
            ),
        )
        setScrollContent(state, scrollState, showBottom)

        runOnIdle { state.start() }
        awaitTip("Top")
        runOnIdle { state.next() }

        awaitTip("Bottom")
        assertTrue(scrollState.value > 0, "the container did not scroll")
        assertTrue(state.isStepVisible)
    }

    @Test
    fun `target mounted by a suspending beforeShow is scrolled into view`() = runComposeUiTest {
        val showBottom = mutableStateOf(false)
        val scrollState = ScrollState(0)
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "bottom",
                    title = "Bottom",
                    beforeShow = {
                        delay(100)
                        showBottom.value = true
                        delay(100)
                    },
                ),
            ),
        )
        setScrollContent(state, scrollState, showBottom)

        runOnIdle { state.start() }

        awaitTip("Bottom")
        assertTrue(scrollState.value > 0, "the container did not scroll")
    }

    @Test
    fun `target that registers after step entry is scrolled into view`() = runComposeUiTest {
        val showBottom = mutableStateOf(false)
        val scrollState = ScrollState(0)
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "bottom", title = "Bottom")),
        )
        setScrollContent(state, scrollState, showBottom)

        runOnIdle { state.start() }
        waitForIdle()
        assertEquals(0, scrollState.value)

        // The app mounts the target some time after the step was entered.
        runOnIdle { showBottom.value = true }

        awaitTip("Bottom")
        assertTrue(scrollState.value > 0, "the container did not scroll")
    }

    @Test
    fun `user scrolling during a PassThrough step is not undone`() = runComposeUiTest {
        val showBottom = mutableStateOf(true)
        val scrollState = ScrollState(0)
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "bottom",
                    title = "Bottom",
                    interaction = TargetInteraction.PassThrough,
                ),
            ),
        )
        setScrollContent(state, scrollState, showBottom)

        runOnIdle { state.start() }
        awaitTip("Bottom")
        val scrolledTo = scrollState.value
        assertTrue(scrolledTo > 0)

        // The user drags the list from inside the target, moving it (and its
        // bounds) around, even out of view.
        onNodeWithTag("bottom").performTouchInput { swipeDown(startY = centerY, endY = centerY + 400f) }
        waitForIdle()
        val userPosition = scrollState.value
        assertTrue(userPosition < scrolledTo, "the swipe did not scroll the list")

        mainClock.advanceTimeBy(1000)
        waitForIdle()

        assertEquals(userPosition, scrollState.value, "the tour scrolled the list back")
    }
}
