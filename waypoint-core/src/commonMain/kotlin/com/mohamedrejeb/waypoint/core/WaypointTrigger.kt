package com.mohamedrejeb.waypoint.core

/**
 * Controls how a step advances to the next one.
 *
 * By default, steps advance when the user clicks the Next button or
 * uses keyboard navigation. Custom triggers allow steps to advance
 * automatically when the user performs a specific action.
 *
 * Example -- advance when user types something:
 * ```kotlin
 * step(Targets.SearchField) {
 *     title = "Try searching"
 *     advanceOn = WaypointTrigger.Custom {
 *         snapshotFlow { searchQuery.value }
 *             .filter { it.isNotEmpty() }
 *             .first()
 *     }
 * }
 * ```
 */
public sealed interface WaypointTrigger {
    /**
     * Default: the step advances when the user clicks Next, presses
     * a keyboard shortcut, or taps the target (if ClickToAdvance is set).
     */
    public data object NextButton : WaypointTrigger

    /**
     * The step advances automatically when the [await] suspend function returns.
     *
     * The function is launched when the step becomes active and cancelled if
     * the step is exited before it returns (e.g., user navigates back or
     * cancels the tour). The Next button and keyboard shortcuts still work
     * alongside the trigger.
     *
     * @param await suspend function that completes when the trigger condition is met
     */
    public data class Custom(
        val await: suspend () -> Unit,
    ) : WaypointTrigger

    public companion object {
        public val Default: WaypointTrigger = NextButton
    }
}
