package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Identifies the nearest [WaypointHost] or [WaypointOverlayHost] in the composition.
 *
 * Targets marked with [waypointTarget] resolve their host via this local and register
 * their bounds against that host's coordinate space. When null, no host is in scope
 * and the target is ignored.
 *
 * Public consumers need this only when calling [WaypointState.setTargetBounds]
 * manually from a composable that isn't wrapped by [Modifier.waypointCanvasTarget]
 * or [Modifier.waypointTarget], for example to register bounds computed inside a
 * `DrawScope`. Read `LocalWaypointHostId.current` inside the composable that owns
 * the drawing to obtain the host id.
 */
@ExperimentalWaypointApi
public val LocalWaypointHostId: androidx.compose.runtime.ProvidableCompositionLocal<Any?> =
    staticCompositionLocalOf<Any?> { null }
