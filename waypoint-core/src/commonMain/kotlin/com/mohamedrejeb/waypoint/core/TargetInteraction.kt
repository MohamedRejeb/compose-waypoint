package com.mohamedrejeb.waypoint.core

/**
 * Controls how the highlighted target responds to pointer input during a tour step.
 *
 * Touch blocking only exists for [HighlightStyle.Spotlight]: every other
 * highlight style leaves the whole screen interactive. A tour that wants
 * blocking without dimming uses `HighlightStyle.Spotlight(overlayAlpha = 0f)`.
 *
 * Ignored for steps without a target.
 */
public enum class TargetInteraction {
    /** Touches on the target are swallowed. */
    None,

    /** Tapping the target advances the tour, the target itself does not receive the tap. */
    ClickToAdvance,

    /**
     * All gestures inside the highlighted areas (the target and every additional
     * target) reach the app, everything outside stays blocked. The interactive
     * areas are the padded bounding rectangles of the targets, regardless of
     * the spotlight shape.
     *
     * While such a step is shown the host does not take keyboard focus and only
     * handles the dismiss keys, so the user can type in the target.
     */
    PassThrough,
}
