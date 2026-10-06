package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifies that onTourComplete / onTourCancel on the primary host fire no
 * matter where the tour-ending action originates: primary host tooltip,
 * overlay host tooltip (Dialog/Sheet), keyboard, or direct state calls.
 */
@OptIn(ExperimentalTestApi::class)
class TourEndCallbackTest {

    private fun twoStepState() = WaypointState(
        steps = listOf(
            WaypointStep(targetKey = "first", title = "Step One"),
            WaypointStep(targetKey = "second", title = "Step Two"),
        ),
    )

    @Test
    fun `onTourComplete fires when tour completes via state next`() = runComposeUiTest {
        val state = twoStepState()
        var completed = 0
        var cancelled = 0

        setContent {
            WaypointHost(
                state = state,
                onTourComplete = { completed++ },
                onTourCancel = { cancelled++ },
                tooltipContent = { _ -> BasicText("Tooltip") },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                    Box(Modifier.size(100.dp).waypointTarget(state, "second"))
                }
            }
        }

        runOnIdle { state.start() }
        runOnIdle { state.next() }
        runOnIdle { state.next() }
        waitForIdle()

        assertEquals(1, completed, "onTourComplete should fire exactly once")
        assertEquals(0, cancelled)
        assertFalse(state.isActive)
    }

    @Test
    fun `onTourCancel fires when state stop is called directly`() = runComposeUiTest {
        val state = twoStepState()
        var completed = 0
        var cancelled = 0

        setContent {
            WaypointHost(
                state = state,
                onTourComplete = { completed++ },
                onTourCancel = { cancelled++ },
                tooltipContent = { _ -> BasicText("Tooltip") },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                }
            }
        }

        runOnIdle { state.start() }
        runOnIdle { state.stop() }
        waitForIdle()

        assertEquals(0, completed)
        assertEquals(1, cancelled, "onTourCancel should fire exactly once")
    }

    @Test
    fun `onTourComplete fires when last step finishes inside an overlay host`() = runComposeUiTest {
        val state = twoStepState()
        var completed = 0
        var overlayScope: StepScope? = null

        setContent {
            WaypointHost(
                state = state,
                onTourComplete = { completed++ },
                tooltipContent = { _ -> BasicText("Primary tooltip") },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "first"))

                    // Simulates a Dialog/Sheet content tree with its own host.
                    WaypointOverlayHost(
                        state = state,
                        tooltipContent = { scope ->
                            overlayScope = scope
                            BasicText("Overlay tooltip", Modifier.testTag("overlay-tooltip"))
                        },
                    ) {
                        Box(Modifier.size(80.dp).waypointTarget(state, "second"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        runOnIdle { state.next() }
        waitUntil(timeoutMillis = 3000) { overlayScope != null }

        // Finish the tour from the overlay host's tooltip (the Dialog case).
        runOnIdle { overlayScope?.next() }
        waitForIdle()

        assertFalse(state.isActive)
        assertEquals(1, completed, "onTourComplete should fire when the tour ends inside an overlay host")
    }

    @Test
    fun `onTourCancel fires when skip happens inside an overlay host`() = runComposeUiTest {
        val state = twoStepState()
        var cancelled = 0
        var overlayScope: StepScope? = null

        setContent {
            WaypointHost(
                state = state,
                onTourCancel = { cancelled++ },
                tooltipContent = { _ -> BasicText("Primary tooltip") },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "first"))

                    WaypointOverlayHost(
                        state = state,
                        tooltipContent = { scope ->
                            overlayScope = scope
                            BasicText("Overlay tooltip")
                        },
                    ) {
                        Box(Modifier.size(80.dp).waypointTarget(state, "second"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        runOnIdle { state.next() }
        waitUntil(timeoutMillis = 3000) { overlayScope != null }

        runOnIdle { overlayScope?.skip() }
        waitForIdle()

        assertFalse(state.isActive)
        assertEquals(1, cancelled, "onTourCancel should fire when skip happens inside an overlay host")
    }

    @Test
    fun `callbacks do not fire when tour never ends`() = runComposeUiTest {
        val state = twoStepState()
        var completed = 0
        var cancelled = 0

        setContent {
            WaypointHost(
                state = state,
                onTourComplete = { completed++ },
                onTourCancel = { cancelled++ },
                tooltipContent = { _ -> BasicText("Tooltip") },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                }
            }
        }

        runOnIdle { state.start() }
        runOnIdle { state.next() }  // move to second step... but second not registered
        waitForIdle()

        // Tour still active (second step pending), nothing ended.
        assertTrue(state.isActive)
        assertEquals(0, completed)
        assertEquals(0, cancelled)
    }
}
