package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * UI tests for [Modifier.waypointCanvasTarget].
 *
 * Verifies that the experimental modifier:
 * - converts canvas-local rects to host-local bounds,
 * - reacts to snapshot state changes on the boundsInCanvas lambda,
 * - clears bounds (keeping host) when the canvas scrolls off screen,
 * - fully unregisters the target on dispose,
 * - is a safe no-op when no WaypointHost is in scope.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalWaypointApi::class)
class WaypointCanvasTargetUiTest {

    @Test
    fun `boundsInCanvas registers host-local bounds`() = runComposeUiTest {
        val state = WaypointState(steps = listOf(WaypointStep(targetKey = "k")))
        var density = 1f

        setContent {
            density = LocalDensity.current.density
            WaypointHost(
                state = state,
                tooltipContent = { _ -> },
            ) {
                Box(
                    modifier = Modifier
                        .offset(40.dp, 50.dp)
                        .size(100.dp, 100.dp)
                        .waypointCanvasTarget(state, "k") {
                            Rect(10f, 20f, 60f, 80f)
                        },
                )
            }
        }

        runOnIdle { state.start() }
        waitForIdle()

        waitUntil(timeoutMillis = 3000) { state.currentTargetBounds != null }

        val bounds = state.currentTargetBounds
        assertNotNull(bounds)
        val expectedLeft = 40f * density + 10f
        val expectedTop = 50f * density + 20f
        assertEquals(expectedLeft, bounds.left, 0.5f)
        assertEquals(expectedTop, bounds.top, 0.5f)
        assertEquals(expectedLeft + 50f, bounds.right, 0.5f) // width 60-10 = 50
        assertEquals(expectedTop + 60f, bounds.bottom, 0.5f) // height 80-20 = 60
    }

    @Test
    fun `graphicsLayer scale on the canvas is captured in registered bounds`() = runComposeUiTest {
        val state = WaypointState(steps = listOf(WaypointStep(targetKey = "k")))

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _ -> },
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = 2f,
                            scaleY = 2f,
                            transformOrigin = TransformOrigin(0f, 0f),
                        )
                        .size(100.dp, 100.dp)
                        .waypointCanvasTarget(state, "k") {
                            Rect(10f, 20f, 60f, 80f)
                        },
                )
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) { state.currentTargetBounds != null }

        val bounds = state.currentTargetBounds
        assertNotNull(bounds)
        // Zoomed 2x from origin: position and size both scale.
        assertEquals(20f, bounds.left, 0.5f)
        assertEquals(40f, bounds.top, 0.5f)
        assertEquals(100f, bounds.width, 0.5f)
        assertEquals(120f, bounds.height, 0.5f)
    }

    @Test
    fun `reactive updates to boundsInCanvas re-register`() = runComposeUiTest {
        val state = WaypointState(steps = listOf(WaypointStep(targetKey = "k")))
        var canvasRect by mutableStateOf(Rect(0f, 0f, 30f, 30f))
        var density = 1f

        setContent {
            density = LocalDensity.current.density
            WaypointHost(
                state = state,
                tooltipContent = { _ -> },
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp, 200.dp)
                        .waypointCanvasTarget(state, "k") { canvasRect },
                )
            }
        }

        runOnIdle { state.start() }
        waitForIdle()

        waitUntil(timeoutMillis = 3000) {
            val b = state.currentTargetBounds
            b != null &&
                kotlin.math.abs(b.left - 0f) < 0.5f &&
                kotlin.math.abs(b.right - 30f) < 0.5f
        }

        // Mutate the snapshot state, modifier should re-register with new bounds.
        runOnIdle { canvasRect = Rect(100f, 120f, 180f, 200f) }

        waitUntil(timeoutMillis = 3000) {
            val b = state.currentTargetBounds
            b != null &&
                kotlin.math.abs(b.left - 100f) < 0.5f &&
                kotlin.math.abs(b.top - 120f) < 0.5f &&
                kotlin.math.abs(b.right - 180f) < 0.5f &&
                kotlin.math.abs(b.bottom - 200f) < 0.5f
        }

        // Suppress unused warning, density proves we're running in Compose scope.
        assertEquals(true, density > 0f)
    }

    @Test
    fun `canvas scrolled off-screen clears bounds but keeps host`() = runComposeUiTest {
        val state = WaypointState(steps = listOf(WaypointStep(targetKey = "k")))
        val scrollState = ScrollState(0)

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _ -> },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .verticalScroll(scrollState),
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp, 100.dp)
                            .waypointCanvasTarget(state, "k") {
                                Rect(0f, 0f, 50f, 50f)
                            },
                    )
                    Spacer(Modifier.height(2000.dp))
                }
            }
        }

        runOnIdle { state.start() }
        waitForIdle()

        // Canvas is visible at the top of the scroll container, so bounds and
        // host id get registered.
        waitUntil(timeoutMillis = 3000) { state.currentTargetBounds != null }
        assertNotNull(state.targetHostIds["k"])

        // Scroll the canvas well above the viewport. Two dispatches: the first
        // changes the scroll state which drives a relayout, the second tick
        // gives onGloballyPositioned another chance to fire so the modifier's
        // snapshotFlow re-observes the now-off-screen bounds.
        runOnIdle { scrollState.dispatchRawDelta(3000f) }
        waitForIdle()
        runOnIdle { scrollState.dispatchRawDelta(10f) }
        waitForIdle()

        // Bounds should clear (canvas no longer overlaps host in root space),
        // but the host association stays since clearTargetBoundsKeepingHost
        // only clears bounds.
        waitUntil(timeoutMillis = 5000) { state.targetCoordinates["k"] == null }

        assertNull(state.targetCoordinates["k"])
        assertNotNull(state.targetHostIds["k"])
    }

    @Test
    fun `disposal unregisters target completely`() = runComposeUiTest {
        val state = WaypointState(steps = listOf(WaypointStep(targetKey = "k")))
        var visible by mutableStateOf(true)

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _ -> },
            ) {
                if (visible) {
                    Box(
                        modifier = Modifier
                            .size(100.dp, 100.dp)
                            .waypointCanvasTarget(state, "k") {
                                Rect(0f, 0f, 50f, 50f)
                            },
                    )
                }
            }
        }

        runOnIdle { state.start() }
        waitForIdle()

        waitUntil(timeoutMillis = 3000) { state.currentTargetBounds != null }
        assertNotNull(state.targetHostIds["k"])

        runOnIdle { visible = false }
        waitForIdle()

        assertNull(state.targetCoordinates["k"])
        assertNull(state.targetHostIds["k"])
    }

    @Test
    fun `setTargetBoundsFromLocal registers translated host-local rect`() = runComposeUiTest {
        val state = WaypointState(steps = listOf(WaypointStep(targetKey = "k")))
        var sourceCoords: LayoutCoordinates? by mutableStateOf(null)

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _ -> },
            ) {
                Box(
                    modifier = Modifier
                        .offset(40.dp, 50.dp)
                        .size(100.dp, 100.dp)
                        .onGloballyPositioned { sourceCoords = it },
                )
                val hid = LocalWaypointHostId.current
                LaunchedEffect(hid, sourceCoords) {
                    val coords = sourceCoords ?: return@LaunchedEffect
                    val id = hid ?: return@LaunchedEffect
                    state.setTargetBoundsFromLocal(
                        key = "k",
                        hostId = id,
                        sourceCoords = coords,
                        localBounds = Rect(10f, 20f, 60f, 80f),
                    )
                }
            }
        }

        runOnIdle { state.start() }
        waitForIdle()
        waitUntil(timeoutMillis = 3000) { state.currentTargetBounds != null }

        val bounds = state.currentTargetBounds
        assertNotNull(bounds)
        // Don't bind to density precisely, just verify translation happened.
        // Source-local origin (10,20)..(60,80) translated through Box at
        // (40.dp, 50.dp) must land in host space offset by the Box position.
        assertEquals(50f, bounds.width, 0.5f) // 60 - 10
        assertEquals(60f, bounds.height, 0.5f) // 80 - 20
        assertTrue(bounds.left > 10f) // was offset by Box position
        assertTrue(bounds.top > 20f)
    }

    @Test
    fun `boundsInCanvas captured as function parameter is updated on recomposition`() = runComposeUiTest {
        val state = WaypointState(steps = listOf(WaypointStep(targetKey = "k")))
        var shape by mutableStateOf<Rect?>(null)

        @Composable
        fun Target(rect: Rect?) {
            Box(
                modifier = Modifier
                    .size(200.dp, 200.dp)
                    .waypointCanvasTarget(state, "k") { rect ?: Rect.Zero },
            )
        }

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _ -> },
            ) {
                Target(shape)
            }
        }

        runOnIdle { state.start() }
        waitForIdle()

        // No shape yet — nothing registered.
        assertNull(state.currentTargetBounds)

        runOnIdle { shape = Rect(0f, 0f, 50f, 60f) }
        waitUntil(timeoutMillis = 3000) { state.currentTargetBounds != null }

        val bounds = state.currentTargetBounds
        assertNotNull(bounds)
        assertEquals(50f, bounds.width, 0.5f)
        assertEquals(60f, bounds.height, 0.5f)
    }

    @Test
    fun `no host in scope is a no-op`() = runComposeUiTest {
        val state = WaypointState(steps = listOf(WaypointStep(targetKey = "k")))

        setContent {
            // No WaypointHost surrounding the Box.
            Box(
                modifier = Modifier
                    .size(100.dp, 100.dp)
                    .waypointCanvasTarget(state, "k") {
                        Rect(0f, 0f, 50f, 50f)
                    },
            )
        }

        runOnIdle { state.start() }
        waitForIdle()

        assertNull(state.targetCoordinates["k"])
        assertNull(state.targetHostIds["k"])
    }
}
