package com.mohamedrejeb.waypoint.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for [WaypointHintState] - dismissal, open/close lifecycle, persistence hydration,
 * and lookup semantics.
 *
 * Uses a [RecordingPersistence] fake that stores completed IDs in a [MutableSet] so tests
 * can assert persistence reads and writes.
 */
class WaypointHintStateTest {

    private enum class HintKey { Search, Filter, Profile }

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

    private fun threeHints(): List<WaypointHint<HintKey>> = listOf(
        WaypointHint(key = HintKey.Search, title = "Search"),
        WaypointHint(key = HintKey.Filter, title = "Filter"),
        WaypointHint(key = HintKey.Profile, title = "Profile"),
    )

    private fun stateWithPersistence(
        groupId: String? = "hints-group",
        persistence: WaypointPersistence? = RecordingPersistence(),
    ): Pair<WaypointHintState<HintKey>, RecordingPersistence?> {
        val p = persistence as? RecordingPersistence
        val state = WaypointHintState(
            hints = threeHints(),
            persistence = persistence,
            groupId = groupId,
        )
        return state to p
    }

    // -- Initial state --

    @Test
    fun `initial state has no dismissed hints and no open key`() {
        val state = WaypointHintState<HintKey>(hints = threeHints())

        assertFalse(state.isDismissed(HintKey.Search))
        assertFalse(state.isDismissed(HintKey.Filter))
        assertFalse(state.isDismissed(HintKey.Profile))
        assertNull(state.openHintKey)
    }

    // -- Dismiss --

    @Test
    fun `dismiss marks the hint as dismissed`() {
        val state = WaypointHintState<HintKey>(hints = threeHints())

        state.dismiss(HintKey.Search)

        assertTrue(state.isDismissed(HintKey.Search))
        assertFalse(state.isDismissed(HintKey.Filter))
    }

    @Test
    fun `dismiss persists via persistence with groupId`() {
        val (state, persistence) = stateWithPersistence()

        state.dismiss(HintKey.Search)

        assertNotNull(persistence)
        assertTrue(
            "hints-group:${HintKey.Search}" in persistence.completed,
            "Persistence should contain the namespaced hint id after dismiss",
        )
    }

    @Test
    fun `dismiss without persistence does not crash`() {
        val state = WaypointHintState<HintKey>(
            hints = threeHints(),
            persistence = null,
            groupId = "hints-group",
        )

        // Should not throw
        state.dismiss(HintKey.Search)
        assertTrue(state.isDismissed(HintKey.Search))
    }

    @Test
    fun `dismiss without groupId persists under the hint prefix`() {
        val persistence = RecordingPersistence()
        val state = WaypointHintState(
            hints = threeHints(),
            persistence = persistence,
            groupId = null,
        )

        state.dismiss(HintKey.Search)

        assertTrue(state.isDismissed(HintKey.Search))
        assertTrue(
            "hint:${HintKey.Search}" in persistence.completed,
            "Without a groupId, dismissal should persist under the hint: prefix",
        )
    }

    // -- Reset --

    @Test
    fun `reset clears dismissed state and resets persistence`() {
        val (state, persistence) = stateWithPersistence()
        state.dismiss(HintKey.Search)
        assertNotNull(persistence)
        assertTrue(state.isDismissed(HintKey.Search))
        assertTrue("hints-group:${HintKey.Search}" in persistence.completed)

        state.reset(HintKey.Search)

        assertFalse(state.isDismissed(HintKey.Search))
        assertFalse("hints-group:${HintKey.Search}" in persistence.completed)
        assertTrue(
            "hints-group:${HintKey.Search}" in persistence.resetCalls,
            "persistence.reset should have been called with the namespaced id",
        )
    }

    @Test
    fun `resetAll clears all dismissed keys and resets all persisted entries`() {
        val (state, persistence) = stateWithPersistence()
        state.dismiss(HintKey.Search)
        state.dismiss(HintKey.Filter)
        assertNotNull(persistence)
        assertTrue(state.isDismissed(HintKey.Search))
        assertTrue(state.isDismissed(HintKey.Filter))

        state.resetAll()

        assertFalse(state.isDismissed(HintKey.Search))
        assertFalse(state.isDismissed(HintKey.Filter))
        assertFalse(state.isDismissed(HintKey.Profile))
        assertTrue(persistence.completed.isEmpty())
        assertTrue(
            "hints-group:${HintKey.Search}" in persistence.resetCalls,
            "persistence.reset should be called for Search",
        )
        assertTrue(
            "hints-group:${HintKey.Filter}" in persistence.resetCalls,
            "persistence.reset should be called for Filter",
        )
    }

    // -- Open / Close --

    @Test
    fun `open sets openHintKey`() {
        val state = WaypointHintState<HintKey>(hints = threeHints())

        state.open(HintKey.Search)

        assertEquals(HintKey.Search, state.openHintKey)
    }

    @Test
    fun `open on a dismissed hint is a no-op`() {
        val state = WaypointHintState<HintKey>(hints = threeHints())
        state.dismiss(HintKey.Search)

        state.open(HintKey.Search)

        assertNull(state.openHintKey)
    }

    @Test
    fun `close clears openHintKey`() {
        val state = WaypointHintState<HintKey>(hints = threeHints())
        state.open(HintKey.Search)
        assertEquals(HintKey.Search, state.openHintKey)

        state.close()

        assertNull(state.openHintKey)
    }

    @Test
    fun `dismiss while open closes the tooltip`() {
        val state = WaypointHintState<HintKey>(hints = threeHints())
        state.open(HintKey.Search)

        state.dismiss(HintKey.Search)

        assertTrue(state.isDismissed(HintKey.Search))
        assertNull(state.openHintKey)
    }

    @Test
    fun `dismiss of different key does not close an open hint`() {
        val state = WaypointHintState<HintKey>(hints = threeHints())
        state.open(HintKey.Search)

        state.dismiss(HintKey.Filter)

        assertEquals(HintKey.Search, state.openHintKey)
        assertTrue(state.isDismissed(HintKey.Filter))
    }

    // -- Hydration --

    @Test
    fun `init hydrates dismissed state from persistence when groupId set`() {
        val persistence = RecordingPersistence()
        persistence.completed += "hints-group:${HintKey.Search}"

        val state = WaypointHintState(
            hints = threeHints(),
            persistence = persistence,
            groupId = "hints-group",
        )

        assertTrue(state.isDismissed(HintKey.Search))
        assertFalse(state.isDismissed(HintKey.Filter))
        assertFalse(state.isDismissed(HintKey.Profile))
    }

    @Test
    fun `init hydrates from persistence without groupId using the hint prefix`() {
        val persistence = RecordingPersistence()
        // Grouped ids are not read when groupId is null, but hint-prefixed ones are.
        persistence.completed += "hints-group:${HintKey.Search}"
        persistence.completed += "hint:${HintKey.Filter}"

        val state = WaypointHintState(
            hints = threeHints(),
            persistence = persistence,
            groupId = null,
        )

        assertFalse(state.isDismissed(HintKey.Search))
        assertTrue(state.isDismissed(HintKey.Filter))
        assertFalse(state.isDismissed(HintKey.Profile))
    }

    // -- Find --

    @Test
    fun `find returns the registered hint`() {
        val hints = threeHints()
        val state = WaypointHintState(hints = hints)

        val found = state.find(HintKey.Filter)

        assertNotNull(found)
        assertSame(hints[1], found)
        assertEquals("Filter", found.title)
    }

    @Test
    fun `find returns null for an unregistered key`() {
        val state = WaypointHintState(
            hints = listOf(
                WaypointHint(key = HintKey.Search, title = "Search"),
            ),
        )

        assertNull(state.find(HintKey.Filter))
        assertNull(state.find(HintKey.Profile))
    }
}
