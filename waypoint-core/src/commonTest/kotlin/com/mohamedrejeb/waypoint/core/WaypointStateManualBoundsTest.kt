package com.mohamedrejeb.waypoint.core

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

@OptIn(ExperimentalWaypointApi::class)
class WaypointStateManualBoundsTest {

    private fun stateWithSteps() = WaypointState(
        steps = listOf(
            WaypointStep(targetKey = "a"),
            WaypointStep(targetKey = "b"),
        ),
    )

    @Test
    fun `setTargetBounds registers bounds and host id`() {
        val state = stateWithSteps()
        val hostId = Any()
        val bounds = Rect(10f, 20f, 110f, 120f)

        state.setTargetBounds("a", hostId, bounds)

        // Tour inactive: currentTargetBounds is null regardless of registration
        assertNull(state.currentTargetBounds)

        state.start()

        assertEquals(bounds, state.currentTargetBounds)
        assertEquals(hostId, state.targetHostIds["a"])
    }

    @Test
    fun `setTargetBounds overwrites existing registration`() {
        val state = stateWithSteps()
        val hostId = Any()
        val first = Rect(0f, 0f, 50f, 50f)
        val second = Rect(100f, 100f, 200f, 200f)

        state.setTargetBounds("a", hostId, first)
        state.setTargetBounds("a", hostId, second)
        state.start()

        assertEquals(second, state.currentTargetBounds)
        assertNotEquals(first, state.currentTargetBounds)
    }

    @Test
    fun `setTargetBounds with different host id reassociates target`() {
        val state = stateWithSteps()
        val hostA = Any()
        val hostB = Any()
        val bounds = Rect(0f, 0f, 50f, 50f)

        state.setTargetBounds("a", hostA, bounds)
        assertEquals(hostA, state.targetHostIds["a"])

        state.setTargetBounds("a", hostB, bounds)
        assertEquals(hostB, state.targetHostIds["a"])
        assertNotEquals(hostA, state.targetHostIds["a"])
    }

    @Test
    fun `clearTargetBounds removes bounds and host id`() {
        val state = stateWithSteps()
        val hostId = Any()
        val bounds = Rect(10f, 20f, 110f, 120f)
        state.setTargetBounds("a", hostId, bounds)

        state.clearTargetBounds("a")

        state.start()
        assertNull(state.currentTargetBounds)
        assertNull(state.targetHostIds["a"])
    }

    @Test
    fun `clearTargetBounds on unregistered key is no-op`() {
        val state = stateWithSteps()

        // Should not throw.
        state.clearTargetBounds("a")

        assertNull(state.targetHostIds["a"])
    }

    @Test
    fun `setTargetBounds does not register BringIntoViewRequester`() {
        val state = stateWithSteps()
        val bounds = Rect(0f, 0f, 50f, 50f)

        state.setTargetBounds("a", Any(), bounds)

        assertNull(state.bringIntoViewRequesters["a"])
    }
}
