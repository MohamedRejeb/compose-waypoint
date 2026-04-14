package com.mohamedrejeb.waypoint.core

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Default values for Waypoint configuration.
 */
public object WaypointDefaults {
    /** Default highlight style */
    public val HighlightStyle: HighlightStyle = com.mohamedrejeb.waypoint.core.HighlightStyle.Default

    /** Default tooltip placement */
    public val TooltipPlacement: TooltipPlacement = com.mohamedrejeb.waypoint.core.TooltipPlacement.Auto

    /** Default overlay click behavior */
    public val OverlayClickBehavior: OverlayClickBehavior =
        com.mohamedrejeb.waypoint.core.OverlayClickBehavior.Nothing

    /** Spacing between the tooltip and the target */
    public val TooltipSpacing: Dp = 12.dp

    /** Minimum margin from screen edges for the tooltip */
    public val ScreenMargin: Dp = 16.dp

    /** Default keyboard navigation config */
    public val KeyboardConfig: KeyboardConfig = com.mohamedrejeb.waypoint.core.KeyboardConfig.Default
}
