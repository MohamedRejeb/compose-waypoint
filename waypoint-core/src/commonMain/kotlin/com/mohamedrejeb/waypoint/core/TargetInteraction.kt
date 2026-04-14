package com.mohamedrejeb.waypoint.core

/**
 * Controls how the target element responds to user interaction during a tour step.
 */
public enum class TargetInteraction {
    /** Target is not interactive during spotlight */
    None,

    /** User can interact with the target normally */
    AllowClick,

    /** Clicking target advances to the next step */
    ClickToAdvance,
}
