package com.mohamedrejeb.waypoint.core

/**
 * Interface for persisting tour completion state.
 *
 * Implement this to remember which tours a user has completed.
 * All methods are synchronous — for async storage, cache the state locally.
 *
 * ```kotlin
 * class SharedPrefsPersistence(prefs: SharedPreferences) : WaypointPersistence {
 *     override fun isCompleted(tourId: String) = prefs.getBoolean("tour_$tourId", false)
 *     override fun markCompleted(tourId: String) { prefs.edit().putBoolean("tour_$tourId", true).apply() }
 *     override fun reset(tourId: String) { prefs.edit().remove("tour_$tourId").apply() }
 *     override fun resetAll() { prefs.edit().clear().apply() }
 * }
 *
 * val state = rememberWaypointState(
 *     tourId = "onboarding",
 *     persistence = SharedPrefsPersistence(prefs),
 * ) { ... }
 * ```
 */
public interface WaypointPersistence {
    /** Returns true if the tour with [tourId] has been completed */
    public fun isCompleted(tourId: String): Boolean

    /** Marks the tour with [tourId] as completed */
    public fun markCompleted(tourId: String)

    /** Resets the completion state for the tour with [tourId] */
    public fun reset(tourId: String)

    /** Resets all tour completion state */
    public fun resetAll()
}
