package com.mohamedrejeb.waypoint.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [WaypointAnalytics] event firing.
 *
 * Uses a [RecordingAnalytics] fake that captures all events in order,
 * so tests can assert the exact sequence and payload of analytics calls.
 */
class AnalyticsTest {

    /** Records all analytics events in order for assertion. */
    private class RecordingAnalytics : WaypointAnalytics {
        data class Event(val type: String, val tourId: String?, val extras: Map<String, Any?>)

        val events = mutableListOf<Event>()

        override fun onTourStarted(tourId: String?, totalSteps: Int) {
            events.add(Event("tourStarted", tourId, mapOf("totalSteps" to totalSteps)))
        }

        override fun onTourCompleted(tourId: String?, totalSteps: Int) {
            events.add(Event("tourCompleted", tourId, mapOf("totalSteps" to totalSteps)))
        }

        override fun onTourCancelled(tourId: String?, stepIndex: Int, totalSteps: Int) {
            events.add(Event("tourCancelled", tourId, mapOf("stepIndex" to stepIndex, "totalSteps" to totalSteps)))
        }

        override fun onStepViewed(tourId: String?, stepIndex: Int, targetKey: Any?) {
            events.add(Event("stepViewed", tourId, mapOf("stepIndex" to stepIndex, "targetKey" to targetKey)))
        }

        override fun onStepCompleted(tourId: String?, stepIndex: Int, targetKey: Any?) {
            events.add(Event("stepCompleted", tourId, mapOf("stepIndex" to stepIndex, "targetKey" to targetKey)))
        }
    }

    private fun threeSteps() = listOf(
        WaypointStep(targetKey = "a"),
        WaypointStep(targetKey = "b"),
        WaypointStep(targetKey = "c"),
    )

    // -- onTourStarted --

    @Test
    fun `onTourStarted fires on start with correct tourId and totalSteps`() {
        val analytics = RecordingAnalytics()
        val state = WaypointState(
            steps = threeSteps(),
            tourId = "onboarding",
            analytics = analytics,
        )

        state.start()

        val startEvent = analytics.events.first { it.type == "tourStarted" }
        assertEquals("onboarding", startEvent.tourId)
        assertEquals(3, startEvent.extras["totalSteps"])
    }

    // -- onStepViewed --

    @Test
    fun `onStepViewed fires on each step transition`() {
        val analytics = RecordingAnalytics()
        val state = WaypointState(steps = threeSteps(), analytics = analytics)

        state.start() // → step 0
        state.next()  // → step 1
        state.next()  // → step 2

        val viewedEvents = analytics.events.filter { it.type == "stepViewed" }
        assertEquals(3, viewedEvents.size)
        assertEquals(0, viewedEvents[0].extras["stepIndex"])
        assertEquals("a", viewedEvents[0].extras["targetKey"])
        assertEquals(1, viewedEvents[1].extras["stepIndex"])
        assertEquals("b", viewedEvents[1].extras["targetKey"])
        assertEquals(2, viewedEvents[2].extras["stepIndex"])
        assertEquals("c", viewedEvents[2].extras["targetKey"])
    }

    // -- onStepCompleted --

    @Test
    fun `onStepCompleted fires when leaving a step`() {
        val analytics = RecordingAnalytics()
        val state = WaypointState(steps = threeSteps(), analytics = analytics)

        state.start() // → step 0
        state.next()  // leave step 0, enter step 1

        val completedEvents = analytics.events.filter { it.type == "stepCompleted" }
        assertEquals(1, completedEvents.size)
        assertEquals(0, completedEvents[0].extras["stepIndex"])
        assertEquals("a", completedEvents[0].extras["targetKey"])
    }

    @Test
    fun `onStepCompleted fires for last step before onTourCompleted`() {
        val analytics = RecordingAnalytics()
        val state = WaypointState(steps = threeSteps(), analytics = analytics)

        state.start()
        state.next()
        state.next()
        state.next() // complete tour from step 2

        // The last few events should be: stepCompleted(2), tourCompleted
        val lastEvents = analytics.events.takeLast(2)
        assertEquals("stepCompleted", lastEvents[0].type)
        assertEquals(2, lastEvents[0].extras["stepIndex"])
        assertEquals("c", lastEvents[0].extras["targetKey"])
        assertEquals("tourCompleted", lastEvents[1].type)
    }

    // -- onTourCompleted --

    @Test
    fun `onTourCompleted fires when last step advances`() {
        val analytics = RecordingAnalytics()
        val state = WaypointState(
            steps = threeSteps(),
            tourId = "feature-tour",
            analytics = analytics,
        )

        state.start()
        state.next()
        state.next()
        state.next() // complete

        val completeEvent = analytics.events.first { it.type == "tourCompleted" }
        assertEquals("feature-tour", completeEvent.tourId)
        assertEquals(3, completeEvent.extras["totalSteps"])
    }

    // -- onTourCancelled --

    @Test
    fun `onTourCancelled fires on stop with correct stepIndex`() {
        val analytics = RecordingAnalytics()
        val state = WaypointState(
            steps = threeSteps(),
            tourId = "onboarding",
            analytics = analytics,
        )

        state.start()
        state.next() // → step 1
        state.stop() // cancel at step 1

        val cancelEvent = analytics.events.first { it.type == "tourCancelled" }
        assertEquals("onboarding", cancelEvent.tourId)
        assertEquals(1, cancelEvent.extras["stepIndex"])
        assertEquals(3, cancelEvent.extras["totalSteps"])
    }

    @Test
    fun `onTourCancelled fires on stop at first step`() {
        val analytics = RecordingAnalytics()
        val state = WaypointState(steps = threeSteps(), analytics = analytics)

        state.start()
        state.stop() // cancel immediately

        val cancelEvent = analytics.events.first { it.type == "tourCancelled" }
        assertEquals(0, cancelEvent.extras["stepIndex"])
    }

    // -- No analytics when null --

    @Test
    fun `no crash when analytics is null`() {
        val state = WaypointState(
            steps = threeSteps(),
            analytics = null, // explicit null
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

    // -- tourId passed through --

    @Test
    fun `tourId is passed to all events`() {
        val analytics = RecordingAnalytics()
        val state = WaypointState(
            steps = threeSteps(),
            tourId = "my-tour",
            analytics = analytics,
        )

        state.start()
        state.next()
        state.stop()

        // Every event should have tourId = "my-tour"
        assertTrue(analytics.events.isNotEmpty())
        analytics.events.forEach { event ->
            assertEquals("my-tour", event.tourId, "Event ${event.type} should have tourId 'my-tour'")
        }
    }

    @Test
    fun `null tourId is passed when not set`() {
        val analytics = RecordingAnalytics()
        val state = WaypointState(
            steps = threeSteps(),
            analytics = analytics,
            // tourId not set — defaults to null
        )

        state.start()
        state.stop()

        analytics.events.forEach { event ->
            assertEquals(null, event.tourId, "Event ${event.type} should have null tourId")
        }
    }

    // -- Full event sequence --

    @Test
    fun `full tour produces correct event sequence`() {
        val analytics = RecordingAnalytics()
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a"),
                WaypointStep(targetKey = "b"),
            ),
            tourId = "seq-test",
            analytics = analytics,
        )

        state.start()  // tourStarted, stepViewed(0)
        state.next()   // stepCompleted(0), stepViewed(1)
        state.next()   // stepCompleted(1), tourCompleted

        val types = analytics.events.map { it.type }
        assertEquals(
            listOf(
                "tourStarted",
                "stepViewed",     // step 0
                "stepCompleted",  // step 0
                "stepViewed",     // step 1
                "stepCompleted",  // step 1
                "tourCompleted",
            ),
            types,
        )
    }

    @Test
    fun `cancel tour produces correct event sequence`() {
        val analytics = RecordingAnalytics()
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a"),
                WaypointStep(targetKey = "b"),
            ),
            analytics = analytics,
        )

        state.start()  // tourStarted, stepViewed(0)
        state.next()   // stepCompleted(0), stepViewed(1)
        state.stop()   // tourCancelled(1)

        val types = analytics.events.map { it.type }
        assertEquals(
            listOf(
                "tourStarted",
                "stepViewed",     // step 0
                "stepCompleted",  // step 0
                "stepViewed",     // step 1
                "tourCancelled",
            ),
            types,
        )
    }
}
