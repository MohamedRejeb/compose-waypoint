package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Auto-scroll brings an off-screen target to the middle of its scroll
 * container, not just to the nearest edge, and leaves a target that is
 * already fully visible where it is.
 *
 * Layout: a 300dp tall scroll container at the top of the window holding
 * [before], a 100dp "first" target, a long spacer, a [targetHeight] "second"
 * target and another long spacer.
 */
@OptIn(ExperimentalTestApi::class)
class AutoScrollCenterUiTest {

    private val containerHeight = 300.dp

    private fun runScrollTest(
        before: Dp = 0.dp,
        targetHeight: Dp = 100.dp,
        block: ComposeUiTest.(WaypointState<String>) -> Unit,
    ) = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "first"), WaypointStep(targetKey = "second")),
        )
        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _ -> BasicText(state.currentStep?.targetKey ?: "") },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(containerHeight)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(Modifier.height(before))
                    Box(Modifier.size(100.dp).waypointTarget(state, "first").testTag("first"))
                    Spacer(Modifier.height(1500.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(targetHeight)
                            .waypointTarget(state, "second")
                            .testTag("second"),
                    )
                    Spacer(Modifier.height(1500.dp))
                }
            }
        }
        runOnIdle { state.start() }
        awaitTooltip("first")
        block(state)
    }

    private fun ComposeUiTest.awaitTooltip(text: String) {
        waitUntil(timeoutMillis = 5000) { onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        waitForIdle()
    }

    private fun ComposeUiTest.topOf(tag: String): Float =
        onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot.top / density.density

    private fun assertNear(expected: Float, actual: Float, message: String) {
        assertTrue(abs(expected - actual) <= 3f, "$message: expected about $expected, was $actual")
    }

    @Test
    fun `off-screen target is scrolled to the middle of its container`() = runScrollTest { state ->
        runOnIdle { state.next() }
        awaitTooltip("second")

        // 100dp target in a 300dp container: centered means its top is at 100dp.
        assertNear(100f, topOf("second"), "top of the centered target")
    }

    @Test
    fun `fully visible target is left where it is`() = runScrollTest(before = 40.dp) { _ ->
        // "first" sits at 40dp, fully visible but not centered.
        assertNear(40f, topOf("first"), "top of the already visible target")
    }

    @Test
    fun `target taller than the container is still brought into view`() = runScrollTest(
        targetHeight = 400.dp,
    ) { state ->
        runOnIdle { state.next() }
        awaitTooltip("second")

        val top = topOf("second")
        assertTrue(top <= 0f && top + 400f >= 300f, "the target should cover the container, top was $top")
    }
}
