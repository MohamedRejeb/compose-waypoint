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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * A canvas target that is scrolled out of view is brought back like any other
 * target, and it is the targeted rectangle inside the canvas that is centered,
 * not the canvas as a whole.
 *
 * Layout: a 300dp tall scroll container holding a 100dp "first" target, a long
 * spacer, a 400dp tall canvas whose target is the 40dp band starting 300dp
 * down, and another long spacer.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalWaypointApi::class)
class CanvasTargetAutoScrollUiTest {

    private fun runCanvasScrollTest(block: ComposeUiTest.(WaypointState<String>) -> Unit) = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "first"), WaypointStep(targetKey = "marker")),
        )
        setContent {
            val density = LocalDensity.current
            WaypointHost(
                state = state,
                tooltipContent = { _ -> BasicText(state.currentStep?.targetKey ?: "") },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                    Spacer(Modifier.height(1500.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .testTag("canvas")
                            .waypointCanvasTarget(state, "marker") {
                                with(density) { Rect(0f, 300.dp.toPx(), 100.dp.toPx(), 340.dp.toPx()) }
                            },
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

    @Test
    fun `off-screen canvas target is scrolled into view`() = runCanvasScrollTest { state ->
        runOnIdle { state.next() }
        awaitTooltip("marker")
    }

    @Test
    fun `the targeted rect is centered, not the canvas`() = runCanvasScrollTest { state ->
        runOnIdle { state.next() }
        awaitTooltip("marker")

        // The 40dp band centered in the 300dp container sits at 130dp, and it
        // starts 300dp down the canvas, so the canvas top is at 130 - 300.
        // positionInRoot, because boundsInRoot is clipped to the visible part.
        val canvasTop = onNodeWithTag("canvas").fetchSemanticsNode().positionInRoot.y / density.density
        assertTrue(abs(canvasTop - (130f - 300f)) <= 3f, "canvas top was $canvasTop")
    }
}
