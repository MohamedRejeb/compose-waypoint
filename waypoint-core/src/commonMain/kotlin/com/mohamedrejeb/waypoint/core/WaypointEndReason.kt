package com.mohamedrejeb.waypoint.core

/**
 * How a tour run ended.
 *
 * Exposed via [WaypointState.lastEndReason] and used by the host to decide
 * whether to invoke `onTourComplete` or `onTourCancel`, and by
 * [WaypointSequenceEffect] to decide whether to advance or halt a sequence.
 */
public enum class WaypointEndReason {
    /** The user advanced past the last visible step */
    Completed,

    /** The tour was stopped/skipped before reaching the end */
    Cancelled,
}
