package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Visual style for a [WaypointBeacon] indicator.
 */
public sealed interface BeaconStyle {
    /**
     * Animated pulsing beacon — a solid dot with expanding ring.
     *
     * @param color the color of both the dot and the expanding pulse ring
     * @param beaconRadius radius of the solid center dot
     * @param maxPulseRadius maximum radius of the expanding ring
     */
    @Immutable
    public data class Pulse(
        val color: Color = Color(0xFFFF4444),
        val beaconRadius: Dp = 5.dp,
        val maxPulseRadius: Dp = 14.dp,
    ) : BeaconStyle

    /**
     * Static dot beacon — no animation, just a colored circle.
     *
     * @param color the dot color
     * @param radius the dot radius
     */
    @Immutable
    public data class Dot(
        val color: Color = Color(0xFFFF4444),
        val radius: Dp = 5.dp,
    ) : BeaconStyle
}
