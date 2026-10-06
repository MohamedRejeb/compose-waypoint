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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * What `Modifier.waypointTarget` registers: bounds clipped by the target's
 * scroll container (so a half scrolled-out target does not open a hole over
 * whatever sits above the list), and a fresh registration when the state
 * instance changes.
 */
@OptIn(ExperimentalTestApi::class)
class WaypointTargetRegistrationUiTest {

    @Test
    fun `bounds of a partially scrolled out target are clipped to the container`() = runComposeUiTest {
        val state = WaypointState(steps = listOf(WaypointStep(targetKey = "item")))
        val scrollState = ScrollState(0)
        var headerHeightPx = 0f
        setContent {
            headerHeightPx = with(LocalDensity.current) { 100.dp.toPx() }
            WaypointHost(state = state, tooltipContent = { _ -> BasicText("Tooltip") }) {
                Column(Modifier.size(300.dp)) {
                    // App bar above the list.
                    Box(Modifier.fillMaxWidth().height(100.dp))
                    Column(Modifier.fillMaxWidth().height(200.dp).verticalScroll(scrollState)) {
                        Box(Modifier.size(100.dp).waypointTarget(state, "item"))
                        Spacer(Modifier.height(1000.dp))
                    }
                }
            }
        }
        waitForIdle()

        // Scroll half of the target under the app bar.
        runOnIdle { scrollState.dispatchRawDelta(50f * (headerHeightPx / 100f)) }
        waitForIdle()

        val bounds = state.targetCoordinates["item"]
        assertNotNull(bounds)
        assertTrue(
            bounds.top >= headerHeightPx - 0.5f,
            "registered bounds reach above the list into the app bar: top=${bounds.top}, bar=$headerHeightPx",
        )
    }

    @Test
    fun `swapping the state instance registers the requester on the new state`() = runComposeUiTest {
        val first = WaypointState(steps = listOf(WaypointStep(targetKey = "a")))
        val second = WaypointState(steps = listOf(WaypointStep(targetKey = "a")))
        var state by mutableStateOf(first)
        setContent {
            WaypointHost(state = state, tooltipContent = { _ -> BasicText("Tooltip") }) {
                Box(Modifier.size(60.dp).waypointTarget(state, "a"))
            }
        }
        waitForIdle()
        assertNotNull(first.bringIntoViewRequesters["a"])

        runOnIdle { state = second }
        waitForIdle()

        assertNotNull(second.bringIntoViewRequesters["a"], "the requester was not registered on the new state")
        assertEquals(null, first.bringIntoViewRequesters["a"], "the old state still holds the target")
    }
}
