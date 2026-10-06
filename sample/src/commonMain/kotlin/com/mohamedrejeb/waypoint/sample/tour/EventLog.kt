package com.mohamedrejeb.waypoint.sample.tour

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mohamedrejeb.waypoint.core.WaypointAnalytics

private const val DefaultCapacity = 8

/** Analytics sink that keeps the latest events, newest first, for display. */
@Stable
class EventLog(private val capacity: Int = DefaultCapacity) : WaypointAnalytics {

    var events: List<String> by mutableStateOf(emptyList())
        private set

    fun add(event: String) {
        events = (listOf(event) + events).take(capacity)
    }

    fun clear() {
        events = emptyList()
    }

    override fun onTourStarted(tourId: String?, totalSteps: Int) =
        add("tour_started $tourId steps=$totalSteps")

    override fun onStepViewed(tourId: String?, stepIndex: Int, targetKey: Any?) =
        add("step_viewed $targetKey")

    override fun onStepCompleted(tourId: String?, stepIndex: Int, targetKey: Any?) =
        add("step_completed $targetKey")

    override fun onTourCompleted(tourId: String?, totalSteps: Int) =
        add("tour_completed $tourId")

    override fun onTourCancelled(tourId: String?, stepIndex: Int, totalSteps: Int) =
        add("tour_cancelled $tourId at=$stepIndex")
}
