package com.mohamedrejeb.waypoint.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [WaypointPersistence] integration with [WaypointState].
 *
 * Uses a [RecordingPersistence] fake that stores completed tour IDs in a [MutableSet],
 * so tests can assert persistence reads and writes.
 */
class PersistenceTest {

    /** In-memory fake that records all persistence operations. */
    private class RecordingPersistence : WaypointPersistence {
        val completed = mutableSetOf<String>()
        override fun isCompleted(tourId: String) = tourId in completed
        override fun markCompleted(tourId: String) { completed += tourId }
        override fun reset(tourId: String) { completed -= tourId }
        override fun resetAll() { completed.clear() }
    }

    private fun stateWithPersistence(
        tourId: String? = "test-tour",
        persistence: WaypointPersistence? = RecordingPersistence(),
    ): Pair<WaypointState<String>, RecordingPersistence?> {
        val p = persistence as? RecordingPersistence
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "A"),
                WaypointStep(targetKey = "B"),
                WaypointStep(targetKey = "C"),
            ),
            tourId = tourId,
            persistence = persistence,
        )
        return state to p
    }

    // -- start skips completed tour --

    @Test
    fun `start skips tour when hasCompleted is true`() {
        val (state, persistence) = stateWithPersistence()
        persistence!!.completed += "test-tour"

        state.start()

        assertFalse(state.isActive, "Tour should not start when already completed")
    }

    // -- start works normally --

    @Test
    fun `start works when hasCompleted is false`() {
        val (state, _) = stateWithPersistence()

        state.start()

        assertTrue(state.isActive, "Tour should start when not completed")
        assertEquals(0, state.currentStepIndex)
    }

    // -- complete marks tour --

    @Test
    fun `complete marks tour as completed`() {
        val (state, persistence) = stateWithPersistence()

        state.start()
        state.next() // A -> B
        state.next() // B -> C
        state.next() // C -> complete

        assertFalse(state.isActive, "Tour should be inactive after completing")
        assertTrue(
            "test-tour" in persistence!!.completed,
            "Persistence should contain the tourId after completion",
        )
    }

    // -- hasCompleted reflects persistence --

    @Test
    fun `hasCompleted reflects persistence state`() {
        val (state, persistence) = stateWithPersistence()

        assertFalse(state.hasCompleted, "hasCompleted should be false initially")

        persistence!!.completed += "test-tour"

        assertTrue(state.hasCompleted, "hasCompleted should be true after marking completed")
    }

    // -- markCompleted writes --

    @Test
    fun `markCompleted writes to persistence`() {
        val (state, persistence) = stateWithPersistence()

        state.markCompleted()

        assertTrue(
            "test-tour" in persistence!!.completed,
            "markCompleted should write tourId to persistence",
        )
    }

    // -- resetCompletion clears --

    @Test
    fun `resetCompletion clears persistence`() {
        val (state, persistence) = stateWithPersistence()
        persistence!!.completed += "test-tour"

        state.resetCompletion()

        assertFalse(
            persistence.isCompleted("test-tour"),
            "resetCompletion should clear the tourId from persistence",
        )
    }

    // -- start works again after reset --

    @Test
    fun `start works again after resetCompletion`() {
        val (state, persistence) = stateWithPersistence()

        // Complete tour
        state.start()
        state.next()
        state.next()
        state.next()
        assertFalse(state.isActive)
        assertTrue("test-tour" in persistence!!.completed)

        // Reset and start again
        state.resetCompletion()
        state.start()

        assertTrue(state.isActive, "Tour should start again after resetCompletion")
        assertEquals(0, state.currentStepIndex)
    }

    // -- null safety --

    @Test
    fun `no crash when persistence is null`() {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "A"),
                WaypointStep(targetKey = "B"),
                WaypointStep(targetKey = "C"),
            ),
            tourId = "test-tour",
            persistence = null,
        )

        // Full lifecycle — nothing should crash
        state.start()
        state.next()
        state.next()
        state.next() // complete

        // Restart and cancel
        state.start()
        state.stop()

        // If we get here without exception, the test passes
        assertTrue(true)
    }

    @Test
    fun `no crash when tourId is null`() {
        val persistence = RecordingPersistence()
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "A"),
                WaypointStep(targetKey = "B"),
                WaypointStep(targetKey = "C"),
            ),
            tourId = null,
            persistence = persistence,
        )

        state.start()

        assertTrue(state.isActive, "Tour should start even with null tourId")

        state.next()
        state.next()
        state.next() // complete

        assertFalse(state.isActive)
        assertTrue(persistence.completed.isEmpty(), "Nothing should be persisted with null tourId")
    }

    @Test
    fun `hasCompleted returns false when tourId is null`() {
        val (state, _) = stateWithPersistence(tourId = null)

        assertFalse(state.hasCompleted, "hasCompleted should return false when tourId is null")
    }

    @Test
    fun `hasCompleted returns false when persistence is null`() {
        val (state, _) = stateWithPersistence(persistence = null)

        assertFalse(state.hasCompleted, "hasCompleted should return false when persistence is null")
    }

    @Test
    fun `complete does not persist when tourId is null`() {
        val persistence = RecordingPersistence()
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "A"),
                WaypointStep(targetKey = "B"),
                WaypointStep(targetKey = "C"),
            ),
            tourId = null,
            persistence = persistence,
        )

        state.start()
        state.next()
        state.next()
        state.next() // complete

        assertTrue(
            persistence.completed.isEmpty(),
            "Persistence should remain empty when tourId is null",
        )
    }

    // -- stop does not mark completed --

    @Test
    fun `stop does not mark tour as completed`() {
        val (state, persistence) = stateWithPersistence()

        state.start()
        state.next() // A -> B
        state.stop() // cancel at B

        assertFalse(
            "test-tour" in persistence!!.completed,
            "Cancelling a tour should NOT mark it as completed",
        )
    }
}
