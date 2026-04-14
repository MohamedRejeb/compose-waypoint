package com.mohamedrejeb.waypoint.core

/**
 * Controls what happens when the user clicks/taps the overlay (outside the spotlight).
 */
public sealed interface OverlayClickBehavior {
    /** Absorb the click, do nothing */
    public data object Nothing : OverlayClickBehavior

    /** Close/cancel the tour */
    public data object Dismiss : OverlayClickBehavior

    /** Advance to the next step */
    public data object NextStep : OverlayClickBehavior

    /** Execute a custom action */
    public data class Custom(val action: () -> Unit) : OverlayClickBehavior
}
