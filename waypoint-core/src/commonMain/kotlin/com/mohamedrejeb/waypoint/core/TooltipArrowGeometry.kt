package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * Geometry for drawing an arrow on a tooltip so it points at the target.
 *
 * Provided by the tooltip popup via [LocalTooltipArrowGeometry] while tooltip
 * content is composed. [arrowOffset] is where the arrow's center should sit:
 * for [ResolvedPlacement.Top]/[ResolvedPlacement.Bottom] it is the distance in
 * px from the tooltip's left edge; for [ResolvedPlacement.Start]/
 * [ResolvedPlacement.End] it is the distance in px from the tooltip's top edge.
 * The offset already accounts for edge clamping, so the arrow keeps pointing at
 * the target even when the tooltip is pushed sideways by a screen edge.
 */
@Immutable
public data class TooltipArrowGeometry(
    val placement: ResolvedPlacement,
    val arrowOffset: Float,
)

/**
 * The arrow geometry for the tooltip currently being composed, or null when
 * content is not inside a Waypoint tooltip popup.
 *
 * Read this in custom tooltip content to draw an arrow pointing at the target,
 * e.g. via [TooltipArrow]. The Material3 tooltip does this automatically.
 */
public val LocalTooltipArrowGeometry: ProvidableCompositionLocal<TooltipArrowGeometry?> =
    compositionLocalOf { null }
