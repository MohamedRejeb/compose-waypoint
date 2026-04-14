package com.mohamedrejeb.waypoint.core

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Defines the shape of the spotlight cutout around the target.
 */
public sealed interface SpotlightShape {
    /** Circular spotlight enclosing the target */
    public data object Circle : SpotlightShape

    /** Rectangular spotlight matching the target bounds */
    public data object Rect : SpotlightShape

    /** Rounded rectangle spotlight with configurable corner radius */
    public data class RoundedRect(val cornerRadius: Dp = 8.dp) : SpotlightShape

    /** Pill/capsule shape (corner radius = half of height) */
    public data object Pill : SpotlightShape

    public companion object {
        public val Default: SpotlightShape = RoundedRect()
    }
}
