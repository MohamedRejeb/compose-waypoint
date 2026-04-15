package com.mohamedrejeb.waypoint.core

/**
 * Interface for tracking tour analytics events.
 *
 * Implement this to send tour engagement data to your analytics backend
 * (e.g., Firebase, Mixpanel, PostHog, custom logging).
 *
 * ```kotlin
 * class MyAnalytics : WaypointAnalytics {
 *     override fun onTourStarted(tourId: String?, totalSteps: Int) {
 *         tracker.log("tour_started", mapOf("tourId" to tourId, "steps" to totalSteps))
 *     }
 *     // ... other events
 * }
 *
 * val state = rememberWaypointState(analytics = MyAnalytics()) { ... }
 * ```
 */
public interface WaypointAnalytics {

    /**
     * Called when a tour starts (first step becomes active).
     *
     * @param tourId optional tour identifier (set via [WaypointState])
     * @param totalSteps total number of steps in the tour
     */
    public fun onTourStarted(tourId: String?, totalSteps: Int) {}

    /**
     * Called when a tour completes (user finishes the last step).
     *
     * @param tourId optional tour identifier
     * @param totalSteps total number of steps
     */
    public fun onTourCompleted(tourId: String?, totalSteps: Int) {}

    /**
     * Called when a tour is cancelled (user skips or stops mid-tour).
     *
     * @param tourId optional tour identifier
     * @param stepIndex the step index where the tour was cancelled
     * @param totalSteps total number of steps
     */
    public fun onTourCancelled(tourId: String?, stepIndex: Int, totalSteps: Int) {}

    /**
     * Called when a step becomes visible (entered).
     *
     * @param tourId optional tour identifier
     * @param stepIndex the index of the step that was viewed
     * @param targetKey the target key of the step
     */
    public fun onStepViewed(tourId: String?, stepIndex: Int, targetKey: Any?) {}

    /**
     * Called when a step is completed (user advances past it).
     *
     * @param tourId optional tour identifier
     * @param stepIndex the index of the step that was completed
     * @param targetKey the target key of the step
     */
    public fun onStepCompleted(tourId: String?, stepIndex: Int, targetKey: Any?) {}
}
