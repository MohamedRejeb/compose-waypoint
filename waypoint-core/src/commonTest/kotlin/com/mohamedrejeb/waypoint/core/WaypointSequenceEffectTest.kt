package com.mohamedrejeb.waypoint.core

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * UI tests for [WaypointSequenceEffect].
 *
 * The effect uses `snapshotFlow` + `LaunchedEffect` so it only runs inside a
 * composition. These tests place the effect inside `setContent`, drive the
 * tour lifecycle from outside, and `waitUntil` to observe the sequence
 * advancing/stopping in response to the tour's `isActive` transitions.
 */
@OptIn(ExperimentalTestApi::class)
class WaypointSequenceEffectTest {

    /** In-memory fake that records all persistence operations. */
    private class RecordingPersistence : WaypointPersistence {
        val completed = mutableSetOf<String>()
        override fun isCompleted(tourId: String) = tourId in completed
        override fun markCompleted(tourId: String) { completed += tourId }
        override fun reset(tourId: String) { completed -= tourId }
        override fun resetAll() { completed.clear() }
    }

    private fun buildTour(
        tourId: String,
        persistence: WaypointPersistence,
        target: String = "foo",
    ): WaypointState<String> = WaypointState(
        steps = listOf(WaypointStep(targetKey = target)),
        tourId = tourId,
        persistence = persistence,
    )

    @Test
    fun `effect auto-advances from tour A to tour B when A completes normally`() = runComposeUiTest {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        setContent {
            WaypointSequenceEffect(sequence)
        }

        runOnIdle { sequence.start() }
        waitForIdle()
        assertTrue(tourA.isActive, "tour A should be active after start")
        assertEquals(0, sequence.activeIndex)

        // Complete tour A. It has one step, so start() + next() completes it.
        runOnIdle { tourA.next() }
        waitForIdle()

        // The snapshotFlow observes tourA.isActive -> false; since hasCompleted
        // is true, the effect calls sequence.advance() which activates tourB.
        waitUntil(timeoutMillis = 3000) { sequence.activeIndex == 1 }

        assertEquals(1, sequence.activeIndex)
        assertTrue(tourB.isActive, "tour B should become active after A completes")
        assertFalse(tourA.isActive)
        assertTrue(persistence.isCompleted("A"))
    }

    @Test
    fun `effect halts the sequence when the current tour is stopped without completing`() = runComposeUiTest {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        setContent {
            WaypointSequenceEffect(sequence)
        }

        runOnIdle { sequence.start() }
        waitForIdle()
        assertTrue(tourA.isActive)
        assertEquals(0, sequence.activeIndex)

        // Cancel tour A (not completed). The effect should call sequence.stop().
        runOnIdle { tourA.stop() }
        waitForIdle()

        waitUntil(timeoutMillis = 3000) { sequence.activeIndex == -1 }

        assertEquals(-1, sequence.activeIndex)
        assertFalse(sequence.isActive)
        assertFalse(tourB.isActive, "tour B should NOT be activated when A is cancelled")
        assertFalse(persistence.isCompleted("A"), "cancelled tour should not be persisted as completed")
    }

    @Test
    fun `effect auto-advances between tours that have no persistence`() = runComposeUiTest {
        // Tours without tourId/persistence must still advance on completion:
        // the effect distinguishes complete vs cancel by how the tour ended,
        // not by persisted completion state.
        val tourA = WaypointState(steps = listOf(WaypointStep(targetKey = "a")))
        val tourB = WaypointState(steps = listOf(WaypointStep(targetKey = "b")))
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        setContent {
            WaypointSequenceEffect(sequence)
        }

        runOnIdle { sequence.start() }
        waitForIdle()
        assertTrue(tourA.isActive)

        // Complete tour A normally (single step, next() completes it).
        runOnIdle { tourA.next() }
        waitForIdle()

        waitUntil(timeoutMillis = 3000) { sequence.activeIndex == 1 }

        assertEquals(1, sequence.activeIndex)
        assertTrue(tourB.isActive, "tour B should become active even without persistence")
    }

    @Test
    fun `effect halts when a tour without persistence is cancelled`() = runComposeUiTest {
        val tourA = WaypointState(steps = listOf(WaypointStep(targetKey = "a")))
        val tourB = WaypointState(steps = listOf(WaypointStep(targetKey = "b")))
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        setContent {
            WaypointSequenceEffect(sequence)
        }

        runOnIdle { sequence.start() }
        waitForIdle()
        assertTrue(tourA.isActive)

        runOnIdle { tourA.stop() }
        waitForIdle()

        waitUntil(timeoutMillis = 3000) { sequence.activeIndex == -1 }

        assertFalse(sequence.isActive)
        assertFalse(tourB.isActive, "cancelling tour A should halt the sequence")
    }

    @Test
    fun `effect advances past an already-completed intermediate tour`() = runComposeUiTest {
        val persistence = RecordingPersistence()
        // Mark B as completed before the sequence runs.
        persistence.completed += "B"
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val tourC = buildTour("C", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB, tourC))

        setContent {
            WaypointSequenceEffect(sequence)
        }

        runOnIdle { sequence.start() }
        waitForIdle()
        assertEquals(0, sequence.activeIndex)
        assertTrue(tourA.isActive)

        // Complete tour A.
        runOnIdle { tourA.next() }
        waitForIdle()

        // Effect observes A -> inactive (completed), calls advance(). advance()
        // skips the already-completed tour B and activates tour C.
        waitUntil(timeoutMillis = 3000) { sequence.activeIndex == 2 }

        assertEquals(2, sequence.activeIndex)
        assertTrue(tourC.isActive)
        assertFalse(tourB.isActive)
    }
}
