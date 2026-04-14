package com.mohamedrejeb.waypoint.core

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Default values for Waypoint configuration.
 */
public object WaypointDefaults {
    /** Default overlay color */
    public val OverlayColor: Color = Color.Black

    /** Default overlay alpha */
    public const val OverlayAlpha: Float = 0.6f

    /** Default spotlight shape */
    public val SpotlightShape: SpotlightShape = com.mohamedrejeb.waypoint.core.SpotlightShape.Default

    /** Default spotlight padding */
    public val SpotlightPadding: SpotlightPadding = com.mohamedrejeb.waypoint.core.SpotlightPadding.Default

    /** Default tooltip placement */
    public val TooltipPlacement: TooltipPlacement = com.mohamedrejeb.waypoint.core.TooltipPlacement.Auto

    /** Default overlay click behavior */
    public val OverlayClickBehavior: OverlayClickBehavior =
        com.mohamedrejeb.waypoint.core.OverlayClickBehavior.Nothing

    /** Spacing between the tooltip and the target */
    public val TooltipSpacing: Dp = 12.dp

    /** Minimum margin from screen edges for the tooltip */
    public val ScreenMargin: Dp = 16.dp
}
