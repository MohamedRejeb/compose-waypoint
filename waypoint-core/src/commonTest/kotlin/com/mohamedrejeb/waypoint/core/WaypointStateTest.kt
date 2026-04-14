package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WaypointStateTest {

    private fun threeSteps() = listOf(
        WaypointStep(targetKey = "a"),
        WaypointStep(targetKey = "b"),
        WaypointStep(targetKey = "c"),
    )

    // -- Lifecycle --

    @Test
    fun `initial state is inactive`() {
        val state = WaypointState(steps = threeSteps())

        assertFalse(state.isActive)
        assertFalse(state.isPaused)
        assertEquals(-1, state.currentStepIndex)
        assertNull(state.currentStep)
    }

    @Test
    fun `start activates tour at first step`() {
        val state = WaypointState(steps = threeSteps())

        state.start()

        assertTrue(state.isActive)
        assertFalse(state.isPaused)
        assertEquals(0, state.currentStepIndex)
        assertEquals("a", state.currentStep?.targetKey)
    }

    @Test
    fun `start is no-op when already active`() {
        val state = WaypointState(steps = threeSteps())
        state.start()
        state.next() // move to step 1

        state.start() // should not reset

        assertEquals(1, state.currentStepIndex)
    }

    @Test
    fun `start with empty steps is no-op`() {
        val state = WaypointState<String>(steps = emptyList())

        state.start()

        assertFalse(state.isActive)
        assertEquals(-1, state.currentStepIndex)
    }

    @Test
    fun `stop deactivates tour`() {
        val state = WaypointState(steps = threeSteps())
        state.start()

        state.stop()

        assertFalse(state.isActive)
        assertFalse(state.isPaused)
        assertEquals(-1, state.currentStepIndex)
        assertNull(state.currentStep)
    }

    @Test
    fun `stop is no-op when not active`() {
        val state = WaypointState(steps = threeSteps())

        state.stop() // should not crash or change state

        assertFalse(state.isActive)
        assertEquals(-1, state.currentStepIndex)
    }

    // -- Navigation --

    @Test
    fun `next advances step index`() {
        val state = WaypointState(steps = threeSteps())
        state.start()

        state.next()

        assertEquals(1, state.currentStepIndex)
        assertEquals("b", state.currentStep?.targetKey)
    }

    @Test
    fun `next on last step completes tour`() {
        val state = WaypointState(steps = threeSteps())
        state.start()
        state.next() // -> 1
        state.next() // -> 2 (last)

        state.next() // should complete

        assertFalse(state.isActive)
        assertEquals(-1, state.currentStepIndex)
    }

    @Test
    fun `next is no-op when not active`() {
        val state = WaypointState(steps = threeSteps())

        state.next()

        assertFalse(state.isActive)
        assertEquals(-1, state.currentStepIndex)
    }

    @Test
    fun `next is no-op when paused`() {
        val state = WaypointState(steps = threeSteps())
        state.start()
        state.pause()

        state.next()

        assertEquals(0, state.currentStepIndex) // didn't advance
    }

    @Test
    fun `previous goes back one step`() {
        val state = WaypointState(steps = threeSteps())
        state.start()
        state.next() // -> 1

        state.previous()

        assertEquals(0, state.currentStepIndex)
        assertEquals("a", state.currentStep?.targetKey)
    }

    @Test
    fun `previous on first step is no-op`() {
        val state = WaypointState(steps = threeSteps())
        state.start()

        state.previous()

        assertEquals(0, state.currentStepIndex)
    }

    @Test
    fun `previous is no-op when not active`() {
        val state = WaypointState(steps = threeSteps())

        state.previous()

        assertFalse(state.isActive)
        assertEquals(-1, state.currentStepIndex)
    }

    @Test
    fun `goTo by index jumps to step`() {
        val state = WaypointState(steps = threeSteps())
        state.start()

        state.goTo(2)

        assertEquals(2, state.currentStepIndex)
        assertEquals("c", state.currentStep?.targetKey)
    }

    @Test
    fun `goTo by key jumps to matching step`() {
        val state = WaypointState(steps = threeSteps())
        state.start()

        state.goTo("c")

        assertEquals(2, state.currentStepIndex)
    }

    @Test
    fun `goTo with out-of-range index is no-op`() {
        val state = WaypointState(steps = threeSteps())
        state.start()

        state.goTo(5)

        assertEquals(0, state.currentStepIndex)
    }

    @Test
    fun `goTo with negative index is no-op`() {
        val state = WaypointState(steps = threeSteps())
        state.start()

        state.goTo(-1)

        assertEquals(0, state.currentStepIndex)
    }

    @Test
    fun `goTo by unknown key is no-op`() {
        val state = WaypointState(steps = threeSteps())
        state.start()

        state.goTo("unknown")

        assertEquals(0, state.currentStepIndex)
    }

    // -- Pause/Resume --

    @Test
    fun `pause sets isPaused flag`() {
        val state = WaypointState(steps = threeSteps())
        state.start()

        state.pause()

        assertTrue(state.isPaused)
        assertTrue(state.isActive)
    }

    @Test
    fun `resume clears isPaused flag`() {
        val state = WaypointState(steps = threeSteps())
        state.start()
        state.pause()

        state.resume()

        assertFalse(state.isPaused)
        assertTrue(state.isActive)
    }

    @Test
    fun `pause preserves step index`() {
        val state = WaypointState(steps = threeSteps())
        state.start()
        state.next() // -> 1

        state.pause()

        assertEquals(1, state.currentStepIndex)
    }

    @Test
    fun `resume continues from paused step`() {
        val state = WaypointState(steps = threeSteps())
        state.start()
        state.next() // -> 1
        state.pause()

        state.resume()

        assertEquals(1, state.currentStepIndex)
        assertTrue(state.isActive)
    }

    @Test
    fun `stop from paused state resets everything`() {
        val state = WaypointState(steps = threeSteps())
        state.start()
        state.next()
        state.pause()

        state.stop()

        assertFalse(state.isActive)
        assertFalse(state.isPaused)
        assertEquals(-1, state.currentStepIndex)
    }

    @Test
    fun `pause when not active is no-op`() {
        val state = WaypointState(steps = threeSteps())

        state.pause()

        assertFalse(state.isPaused)
    }

    @Test
    fun `resume when not paused is no-op`() {
        val state = WaypointState(steps = threeSteps())
        state.start()

        state.resume() // not paused, should be no-op

        assertTrue(state.isActive)
        assertFalse(state.isPaused)
    }

    @Test
    fun `previous is no-op when paused`() {
        val state = WaypointState(steps = threeSteps())
        state.start()
        state.next() // -> 1
        state.pause()

        state.previous()

        assertEquals(1, state.currentStepIndex)
    }

    @Test
    fun `goTo is no-op when paused`() {
        val state = WaypointState(steps = threeSteps())
        state.start()
        state.pause()

        state.goTo(2)

        assertEquals(0, state.currentStepIndex)
    }

    // -- Conditional Steps --

    @Test
    fun `next skips steps where showIf returns false`() {
        val steps = listOf(
            WaypointStep(targetKey = "a"),
            WaypointStep(targetKey = "b", showIf = { false }),
            WaypointStep(targetKey = "c"),
        )
        val state = WaypointState(steps = steps)
        state.start()

        state.next()

        assertEquals(2, state.currentStepIndex)
        assertEquals("c", state.currentStep?.targetKey)
    }

    @Test
    fun `previous skips hidden steps`() {
        val steps = listOf(
            WaypointStep(targetKey = "a"),
            WaypointStep(targetKey = "b", showIf = { false }),
            WaypointStep(targetKey = "c"),
        )
        val state = WaypointState(steps = steps)
        state.start()
        state.next() // skips b -> 2

        state.previous()

        assertEquals(0, state.currentStepIndex)
        assertEquals("a", state.currentStep?.targetKey)
    }

    @Test
    fun `start skips to first visible step`() {
        val steps = listOf(
            WaypointStep(targetKey = "a", showIf = { false }),
            WaypointStep(targetKey = "b"),
            WaypointStep(targetKey = "c"),
        )
        val state = WaypointState(steps = steps)

        state.start()

        assertEquals(1, state.currentStepIndex)
        assertEquals("b", state.currentStep?.targetKey)
    }

    @Test
    fun `all steps hidden results in no-op start`() {
        val steps = listOf(
            WaypointStep(targetKey = "a", showIf = { false }),
            WaypointStep(targetKey = "b", showIf = { false }),
        )
        val state = WaypointState(steps = steps)

        state.start()

        assertFalse(state.isActive)
        assertEquals(-1, state.currentStepIndex)
    }

    @Test
    fun `goTo to hidden step is no-op`() {
        val steps = listOf(
            WaypointStep(targetKey = "a"),
            WaypointStep(targetKey = "b", showIf = { false }),
            WaypointStep(targetKey = "c"),
        )
        val state = WaypointState(steps = steps)
        state.start()

        state.goTo(1) // step b is hidden

        assertEquals(0, state.currentStepIndex)
    }

    @Test
    fun `next completes tour when remaining steps are all hidden`() {
        val steps = listOf(
            WaypointStep(targetKey = "a"),
            WaypointStep(targetKey = "b", showIf = { false }),
            WaypointStep(targetKey = "c", showIf = { false }),
        )
        val state = WaypointState(steps = steps)
        state.start()

        state.next()

        assertFalse(state.isActive)
        assertEquals(-1, state.currentStepIndex)
    }

    // -- Callbacks --

    @Test
    fun `onEnter fires when step becomes active`() {
        var entered = false
        val steps = listOf(
            WaypointStep(targetKey = "a", onEnter = { entered = true }),
        )
        val state = WaypointState(steps = steps)

        state.start()

        assertTrue(entered)
    }

    @Test
    fun `onExit fires when leaving step`() {
        var exited = false
        val steps = listOf(
            WaypointStep(targetKey = "a", onExit = { exited = true }),
            WaypointStep(targetKey = "b"),
        )
        val state = WaypointState(steps = steps)
        state.start()

        state.next()

        assertTrue(exited)
    }

    @Test
    fun `onExit fires on stop`() {
        var exited = false
        val steps = listOf(
            WaypointStep(targetKey = "a", onExit = { exited = true }),
            WaypointStep(targetKey = "b"),
        )
        val state = WaypointState(steps = steps)
        state.start()

        state.stop()

        assertTrue(exited)
    }

    @Test
    fun `onEnter fires on next step`() {
        var enteredB = false
        val steps = listOf(
            WaypointStep(targetKey = "a"),
            WaypointStep(targetKey = "b", onEnter = { enteredB = true }),
        )
        val state = WaypointState(steps = steps)
        state.start()

        state.next()

        assertTrue(enteredB)
    }

    @Test
    fun `onExit and onEnter fire in order during transition`() {
        val events = mutableListOf<String>()
        val steps = listOf(
            WaypointStep(
                targetKey = "a",
                onExit = { events.add("exit-a") },
            ),
            WaypointStep(
                targetKey = "b",
                onEnter = { events.add("enter-b") },
            ),
        )
        val state = WaypointState(steps = steps)
        state.start()

        state.next()

        assertEquals(listOf("exit-a", "enter-b"), events)
    }

    // -- Target Coordinates --

    @Test
    fun `registerTarget stores bounds`() {
        val state = WaypointState(steps = threeSteps())
        val bounds = Rect(10f, 20f, 100f, 80f)

        state.registerTarget("a", bounds)

        assertEquals(bounds, state.targetCoordinates["a"])
    }

    @Test
    fun `unregisterTarget removes bounds`() {
        val state = WaypointState(steps = threeSteps())
        state.registerTarget("a", Rect(10f, 20f, 100f, 80f))

        state.unregisterTarget("a")

        assertNull(state.targetCoordinates["a"])
    }

    @Test
    fun `currentTargetBounds returns bounds for active step`() {
        val state = WaypointState(steps = threeSteps())
        val bounds = Rect(10f, 20f, 100f, 80f)
        state.registerTarget("a", bounds)
        state.start()

        assertEquals(bounds, state.currentTargetBounds)
    }

    @Test
    fun `currentTargetBounds is null when step target not registered`() {
        val state = WaypointState(steps = threeSteps())
        state.start()

        assertNull(state.currentTargetBounds)
    }

    @Test
    fun `currentTargetBounds is null when inactive`() {
        val state = WaypointState(steps = threeSteps())
        state.registerTarget("a", Rect(10f, 20f, 100f, 80f))

        assertNull(state.currentTargetBounds)
    }

    @Test
    fun `currentTargetBounds updates when navigating to next step`() {
        val state = WaypointState(steps = threeSteps())
        val boundsA = Rect(10f, 20f, 100f, 80f)
        val boundsB = Rect(200f, 300f, 400f, 500f)
        state.registerTarget("a", boundsA)
        state.registerTarget("b", boundsB)
        state.start()

        assertEquals(boundsA, state.currentTargetBounds)

        state.next()

        assertEquals(boundsB, state.currentTargetBounds)
    }

    // -- BringIntoViewRequester Registration --

    @Test
    fun `registerBringIntoViewRequester stores requester`() {
        val state = WaypointState(steps = threeSteps())
        val requester = BringIntoViewRequester()

        state.registerBringIntoViewRequester("a", requester)

        assertEquals(requester, state.bringIntoViewRequesters["a"])
    }

    @Test
    fun `unregisterTarget also removes BringIntoViewRequester`() {
        val state = WaypointState(steps = threeSteps())
        state.registerTarget("a", Rect(10f, 20f, 100f, 80f))
        state.registerBringIntoViewRequester("a", BringIntoViewRequester())

        state.unregisterTarget("a")

        assertNull(state.targetCoordinates["a"])
        assertNull(state.bringIntoViewRequesters["a"])
    }

    @Test
    fun `unregisterTarget is safe when no requester registered`() {
        val state = WaypointState(steps = threeSteps())
        state.registerTarget("a", Rect(10f, 20f, 100f, 80f))

        state.unregisterTarget("a") // should not crash

        assertNull(state.targetCoordinates["a"])
    }
}
