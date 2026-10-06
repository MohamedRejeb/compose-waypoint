package com.mohamedrejeb.waypoint.sample.tour

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mohamedrejeb.waypoint.core.WaypointPersistence

/**
 * Keeps completed tour ids for the lifetime of the app. A real app would
 * back this with DataStore or its own settings storage.
 */
class InMemoryPersistence(initial: Set<String> = emptySet()) : WaypointPersistence {

    // Snapshot state, so UI that reads hasCompleted updates when it changes.
    private var completed by mutableStateOf(initial)

    /** What has been completed so far, for saving and restoring. */
    val completedIds: Set<String>
        get() = completed

    override fun isCompleted(tourId: String): Boolean = tourId in completed

    override fun markCompleted(tourId: String) {
        completed = completed + tourId
    }

    override fun reset(tourId: String) {
        completed = completed - tourId
    }

    override fun resetAll() {
        completed = emptySet()
    }
}
