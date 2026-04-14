package com.mohamedrejeb.waypoint.core

/**
 * Defines where the tooltip should be placed relative to the target.
 */
public enum class TooltipPlacement {
    /** Above the target */
    Top,

    /** Below the target */
    Bottom,

    /** To the start side of the target (left in LTR, right in RTL) */
    Start,

    /** To the end side of the target (right in LTR, left in RTL) */
    End,

    /** Automatically choose the side with the most available space */
    Auto,
}
