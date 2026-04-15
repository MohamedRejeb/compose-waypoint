package com.mohamedrejeb.waypoint.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the `beforeShow` async-gate feature.
 *
 * `beforeShow` is a suspend function on [WaypointStep] that runs before the
 * step's highlight/tooltip are displayed. While it is running, [WaypointState.isStepReady]
 * is false; when it completes (or if it is null), `isStepReady` is true.
 *
 * These are pure unit tests — they verify state transitions on [WaypointState]
 * without a Compose UI test harness.
 */
class BeforeShowTest {

    // -- Helpers --

    private fun twoStepsNoBeforeShow() = listOf(
        WaypointStep(targetKey = "a"),
        WaypointStep(targetKey = "b"),
    )

    private fun twoStepsSecondHasBeforeShow() = listOf(
        WaypointStep(targetKey = "a"),
        WaypointStep(targetKey = "b", beforeShow = { /* no-op suspend */ }),
    )

    private fun singleStepWithBeforeShow() = listOf(
        WaypointStep(targetKey = "a", beforeShow = { /* no-op suspend */ }),
    )

    // -- Test 1: step without beforeShow has isStepReady true --

    @Test
    fun `step without beforeShow has isStepReady true`() {
        val state = WaypointState(steps = twoStepsNoBeforeShow())

        state.start()

        assertTrue(state.isStepReady, "isStepReady should be true when step has no beforeShow")
    }

    // -- Test 2: step with beforeShow has isStepReady false after transition --

    @Test
    fun `step with beforeShow has isStepReady false after transition`() {
        val state = WaypointState(steps = twoStepsSecondHasBeforeShow())

        state.start()  // step 0 — no beforeShow
        state.next()   // step 1 — has beforeShow

        assertFalse(
            state.isStepReady,
            "isStepReady should be false immediately after navigating to a step with beforeShow",
        )
    }

    // -- Test 3: isStepReady resets to true on stop --

    @Test
    fun `isStepReady resets to true on stop`() {
        val state = WaypointState(steps = twoStepsSecondHasBeforeShow())

        state.start()
        state.next()   // step 1 — beforeShow sets isStepReady = false
        assertFalse(state.isStepReady, "Precondition: isStepReady should be false")

        state.stop()

        assertTrue(
            state.isStepReady,
            "isStepReady should reset to true when the tour is stopped",
        )
    }

    // -- Test 4: isStepReady is true when beforeShow is null --

    @Test
    fun `isStepReady is true when beforeShow is null`() {
        val state = WaypointState(steps = twoStepsNoBeforeShow())

        state.start()
        state.next()

        assertTrue(
            state.isStepReady,
            "isStepReady should remain true when navigating to a step without beforeShow",
        )
    }

    // -- Test 5: beforeShow lambda is stored in step --

    @Test
    fun `beforeShow lambda is stored in step`() {
        val builder = WaypointStepBuilder<String>()
        builder.step("target") {
            beforeShow { /* async work */ }
        }

        val step = builder.build().single()

        assertNotNull(step.beforeShow, "beforeShow should be stored in the built step")
    }

    // -- Test 6: beforeShow defaults to null --

    @Test
    fun `beforeShow defaults to null`() {
        val builder = WaypointStepBuilder<String>()
        builder.step("target")

        val step = builder.build().single()

        assertNull(step.beforeShow, "beforeShow should default to null when not set in DSL")
    }

    // -- Test 7: first step with beforeShow sets isStepReady false on start --

    @Test
    fun `first step with beforeShow sets isStepReady false on start`() {
        val state = WaypointState(steps = singleStepWithBeforeShow())

        state.start()

        assertFalse(
            state.isStepReady,
            "isStepReady should be false when the first step has beforeShow",
        )
    }

    // -- Test 8: isStepReady resets to true on complete --

    @Test
    fun `isStepReady resets to true on complete`() {
        val steps = listOf(
            WaypointStep(targetKey = "a", beforeShow = { /* async work */ }),
        )
        val state = WaypointState(steps = steps)

        state.start()
        // Simulate that beforeShow has completed (WaypointHost would call setStepReady)
        state.setStepReady(true)

        state.next() // single-step tour — next() completes the tour

        assertFalse(state.isActive, "Tour should be complete after next() on single step")
        assertTrue(
            state.isStepReady,
            "isStepReady should reset to true when the tour completes",
        )
    }
}
