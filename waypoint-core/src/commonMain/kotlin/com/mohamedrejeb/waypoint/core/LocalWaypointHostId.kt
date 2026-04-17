package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Identifies the nearest [WaypointHost] or [WaypointOverlayHost] in the composition.
 *
 * Targets marked with [waypointTarget] resolve their host via this local and register
 * their bounds against that host's coordinate space. When null, no host is in scope
 * and the target is ignored.
 */
internal val LocalWaypointHostId = staticCompositionLocalOf<Any?> { null }
