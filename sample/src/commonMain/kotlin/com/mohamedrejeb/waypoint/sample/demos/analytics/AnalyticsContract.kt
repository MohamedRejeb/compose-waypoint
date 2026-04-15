package com.mohamedrejeb.waypoint.sample.demos.analytics

data class AnalyticsState(
    val events: List<AnalyticsLogEntry> = emptyList(),
)

data class AnalyticsLogEntry(
    val index: Int,
    val type: String,
    val details: String,
)

sealed interface AnalyticsEvent {
    data class LogEvent(val type: String, val details: String) : AnalyticsEvent
    data object ClearLog : AnalyticsEvent
}
