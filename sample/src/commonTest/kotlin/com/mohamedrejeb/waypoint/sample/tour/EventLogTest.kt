package com.mohamedrejeb.waypoint.sample.tour

import kotlin.test.Test
import kotlin.test.assertEquals

class EventLogTest {

    @Test
    fun newestEventComesFirst() {
        val log = EventLog()
        log.add("one")
        log.add("two")
        assertEquals(listOf("two", "one"), log.events)
    }

    @Test
    fun oldestEventIsDroppedPastCapacity() {
        val log = EventLog(capacity = 2)
        log.add("one")
        log.add("two")
        log.add("three")
        assertEquals(listOf("three", "two"), log.events)
    }

    @Test
    fun analyticsCallbacksAreLogged() {
        val log = EventLog()
        log.onTourStarted("x", 3)
        log.onStepViewed("x", 0, "Search")
        log.onStepCompleted("x", 0, "Search")
        log.onTourCancelled("x", 1, 3)
        log.onTourCompleted("x", 3)
        assertEquals(
            listOf(
                "tour_completed x",
                "tour_cancelled x at=1",
                "step_completed Search",
                "step_viewed Search",
                "tour_started x steps=3",
            ),
            log.events,
        )
    }

    @Test
    fun clearEmptiesTheLog() {
        val log = EventLog()
        log.add("one")
        log.clear()
        assertEquals(emptyList(), log.events)
    }
}
