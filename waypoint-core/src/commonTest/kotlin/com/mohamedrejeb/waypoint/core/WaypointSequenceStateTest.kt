package com.mohamedrejeb.waypoint.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for [WaypointSequenceState] - the orchestrator that runs a linear
 * list of [WaypointState] tours.
 *
 * Each test uses a shared [RecordingPersistence] fake to observe persistence
 * reads/writes and constructs [WaypointState] instances directly (the
 * constructor is public API).
 */
class WaypointSequenceStateTest {

    /** In-memory fake that records all persistence operations. */
    private class RecordingPersistence : WaypointPersistence {
        val completed = mutableSetOf<String>()
        val resetCalls = mutableListOf<String>()
        var resetAllCalls = 0

        override fun isCompleted(tourId: String) = tourId in completed
        override fun markCompleted(tourId: String) { completed += tourId }
        override fun reset(tourId: String) {
            completed -= tourId
            resetCalls += tourId
        }
        override fun resetAll() {
            completed.clear()
            resetAllCalls++
        }
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

    // -- Construction --

    @Test
    fun `initial state is inactive with activeIndex -1 when no tour is active at construction`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)

        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        assertEquals(-1, sequence.activeIndex)
        assertFalse(sequence.isActive)
        assertNull(sequence.currentTour)
    }

    @Test
    fun `init derives activeIndex from a tour that is already active before construction`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)

        // Start tour A manually before constructing the sequence. This mimics
        // a configuration change where rememberSaveable restored the tour as
        // active and the sequence is rebuilt on recomposition.
        tourA.start()
        assertTrue(tourA.isActive)

        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        assertEquals(0, sequence.activeIndex)
        assertTrue(sequence.isActive)
        assertSame(tourA, sequence.currentTour)
    }

    // -- start --

    @Test
    fun `start activates the first incomplete tour`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        sequence.start()

        assertEquals(0, sequence.activeIndex)
        assertTrue(tourA.isActive)
        assertFalse(tourB.isActive)
    }

    @Test
    fun `start skips completed tours`() {
        val persistence = RecordingPersistence()
        persistence.completed += "A"
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        sequence.start()

        assertEquals(1, sequence.activeIndex)
        assertFalse(tourA.isActive)
        assertTrue(tourB.isActive)
    }

    @Test
    fun `start is no-op when all tours are completed`() {
        val persistence = RecordingPersistence()
        persistence.completed += "A"
        persistence.completed += "B"
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        sequence.start()

        assertEquals(-1, sequence.activeIndex)
        assertFalse(sequence.isActive)
        assertTrue(sequence.isCompleted)
    }

    @Test
    fun `start is no-op when already active`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        sequence.start()
        val indexAfterFirst = sequence.activeIndex
        val tourAfterFirst = sequence.currentTour

        sequence.start()

        assertEquals(indexAfterFirst, sequence.activeIndex)
        assertSame(tourAfterFirst, sequence.currentTour)
        assertTrue(tourA.isActive)
        assertFalse(tourB.isActive)
    }

    // -- advance --

    @Test
    fun `advance moves to next incomplete tour`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        sequence.start()
        sequence.advance()

        assertEquals(1, sequence.activeIndex)
        assertTrue(tourB.isActive)
    }

    @Test
    fun `advance skips completed tours between current and next`() {
        val persistence = RecordingPersistence()
        persistence.completed += "B"
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val tourC = buildTour("C", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB, tourC))

        sequence.start()
        sequence.advance()

        assertEquals(2, sequence.activeIndex)
        assertFalse(tourB.isActive)
        assertTrue(tourC.isActive)
    }

    @Test
    fun `advance goes inactive when no more tours`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        sequence.start()
        sequence.advance()
        sequence.advance()

        assertEquals(-1, sequence.activeIndex)
        assertFalse(sequence.isActive)
    }

    @Test
    fun `advance is no-op when sequence is inactive`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        sequence.advance()

        assertEquals(-1, sequence.activeIndex)
        assertFalse(tourA.isActive)
        assertFalse(tourB.isActive)
    }

    // -- stop --

    @Test
    fun `stop flips activeIndex to -1 and stops the current tour`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        sequence.start()
        assertTrue(tourA.isActive)

        sequence.stop()

        assertEquals(-1, sequence.activeIndex)
        assertFalse(sequence.isActive)
        assertFalse(tourA.isActive)
    }

    @Test
    fun `stop is no-op when sequence is inactive`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        // Should not throw and should not try to stop a tour that was never started.
        sequence.stop()

        assertEquals(-1, sequence.activeIndex)
        assertFalse(tourA.isActive)
        assertFalse(tourB.isActive)
    }

    // -- goTo --

    @Test
    fun `goTo activates the specified tour if not completed`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val tourC = buildTour("C", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB, tourC))

        sequence.goTo(tourC)

        assertEquals(2, sequence.activeIndex)
        assertTrue(tourC.isActive)
        assertFalse(tourA.isActive)
        assertFalse(tourB.isActive)
    }

    @Test
    fun `goTo is no-op if the tour is not in the list`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val orphan = buildTour("orphan", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        sequence.goTo(orphan)

        assertEquals(-1, sequence.activeIndex)
        assertFalse(orphan.isActive)
        assertFalse(tourA.isActive)
        assertFalse(tourB.isActive)
    }

    @Test
    fun `goTo is no-op if the tour is already completed`() {
        val persistence = RecordingPersistence()
        persistence.completed += "B"
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        sequence.goTo(tourB)

        assertEquals(-1, sequence.activeIndex)
        assertFalse(tourB.isActive)
    }

    @Test
    fun `goTo stops the previously-active tour before starting the new one`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        sequence.start()
        assertTrue(tourA.isActive)

        sequence.goTo(tourB)

        assertEquals(1, sequence.activeIndex)
        assertFalse(tourA.isActive)
        assertTrue(tourB.isActive)
    }

    // -- reset --

    @Test
    fun `reset clears each tour's completion and halts the sequence`() {
        val persistence = RecordingPersistence()
        persistence.completed += "A"
        persistence.completed += "B"
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        sequence.reset()

        assertEquals(-1, sequence.activeIndex)
        assertFalse(sequence.isActive)
        assertTrue("A" in persistence.resetCalls, "persistence.reset should be called for tour A")
        assertTrue("B" in persistence.resetCalls, "persistence.reset should be called for tour B")
        assertFalse(persistence.isCompleted("A"))
        assertFalse(persistence.isCompleted("B"))
    }

    // -- isCompleted --

    @Test
    fun `isCompleted is false when no tours exist`() {
        val sequence = WaypointSequenceState(emptyList())

        assertFalse(sequence.isCompleted)
    }

    @Test
    fun `isCompleted reflects all tours' hasCompleted`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        assertFalse(sequence.isCompleted)

        persistence.completed += "A"
        assertFalse(sequence.isCompleted)

        persistence.completed += "B"
        assertTrue(sequence.isCompleted)
    }

    // -- currentTour --

    @Test
    fun `currentTour returns null when inactive`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        assertNull(sequence.currentTour)
    }

    @Test
    fun `currentTour reflects activeIndex`() {
        val persistence = RecordingPersistence()
        val tourA = buildTour("A", persistence)
        val tourB = buildTour("B", persistence)
        val sequence = WaypointSequenceState(listOf(tourA, tourB))

        sequence.start()
        assertSame(tourA, sequence.currentTour)

        sequence.advance()
        assertSame(tourB, sequence.currentTour)

        sequence.advance()
        assertNull(sequence.currentTour)
    }
}
