package com.mohamedrejeb.waypoint.sample.demos.analytics

import androidx.lifecycle.ViewModel
import com.mohamedrejeb.waypoint.core.WaypointAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class AnalyticsViewModel : ViewModel(), WaypointAnalytics {
    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state
    private var nextIndex = 0

    private fun log(type: String, details: String) {
        _state.update { current ->
            current.copy(events = current.events + AnalyticsLogEntry(nextIndex++, type, details))
        }
    }

    fun onEvent(event: AnalyticsEvent) {
        when (event) {
            is AnalyticsEvent.ClearLog -> {
                _state.update { it.copy(events = emptyList()) }
                nextIndex = 0
            }
            is AnalyticsEvent.LogEvent -> log(event.type, event.details)
        }
    }

    override fun onTourStarted(tourId: String?, totalSteps: Int) {
        log("Tour Started", "tourId=$tourId, steps=$totalSteps")
    }

    override fun onTourCompleted(tourId: String?, totalSteps: Int) {
        log("Tour Completed", "tourId=$tourId, steps=$totalSteps")
    }

    override fun onTourCancelled(tourId: String?, stepIndex: Int, totalSteps: Int) {
        log("Tour Cancelled", "at step ${stepIndex + 1}/$totalSteps")
    }

    override fun onStepViewed(tourId: String?, stepIndex: Int, targetKey: Any?) {
        log("Step Viewed", "step=${stepIndex + 1}, key=$targetKey")
    }

    override fun onStepCompleted(tourId: String?, stepIndex: Int, targetKey: Any?) {
        log("Step Completed", "step=${stepIndex + 1}, key=$targetKey")
    }
}
