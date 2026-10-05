package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Lays out [content] with an arrow that points at the current step's or hint's target.
 * Renders bare content when composed outside a Waypoint tooltip popup or for a step without a target.
 *
 * Use it as the root of custom tooltip content to get an arrow in one call:
 *
 * ```kotlin
 * WaypointHost(
 *     state = state,
 *     tooltipContent = { scope ->
 *         TooltipArrowBox(arrowColor = Color.White) {
 *             MyTooltipCard(scope)
 *         }
 *     },
 * ) { ... }
 * ```
 *
 * The arrow sits on the edge of [content] that faces the target and is
 * positioned from [LocalTooltipArrowGeometry], so it keeps pointing at the
 * target when the tooltip is pushed sideways by a screen edge. For fully
 * custom drawing use [TooltipArrow] and [LocalTooltipArrowGeometry] directly.
 *
 * @param arrowColor arrow fill color, typically the tooltip background color
 * @param modifier modifier for the layout holding the arrow and [content]
 * @param arrowSize how far the arrow protrudes from the tooltip edge; its base
 *   along the edge is twice this
 * @param content the tooltip body
 */
@Composable
public fun TooltipArrowBox(
    arrowColor: Color,
    modifier: Modifier = Modifier,
    arrowSize: Dp = 10.dp,
    content: @Composable () -> Unit,
) {
    val geometry = LocalTooltipArrowGeometry.current
    if (geometry == null) {
        Box(modifier = modifier) { content() }
        return
    }

    val arrowSizePx = with(LocalDensity.current) { arrowSize.toPx() }
    // The arrow composable is 2 * arrowSize long along the tooltip edge, so
    // shift by one arrowSize to center it on the geometry offset.
    val alongEdgeOffset = (geometry.arrowOffset - arrowSizePx).roundToInt()

    when (geometry.placement) {
        // Tooltip below the target: arrow sits on the top edge pointing up.
        ResolvedPlacement.Bottom -> Column(modifier = modifier) {
            TooltipArrow(
                placement = ResolvedPlacement.Bottom,
                color = arrowColor,
                size = arrowSize,
                modifier = Modifier
                    .align(AbsoluteAlignment.Left)
                    .offset { IntOffset(alongEdgeOffset, 0) }
                    .size(width = arrowSize * 2, height = arrowSize),
            )
            content()
        }

        // Tooltip above the target: arrow on the bottom edge pointing down.
        ResolvedPlacement.Top -> Column(modifier = modifier) {
            content()
            TooltipArrow(
                placement = ResolvedPlacement.Top,
                color = arrowColor,
                size = arrowSize,
                modifier = Modifier
                    .align(AbsoluteAlignment.Left)
                    .offset { IntOffset(alongEdgeOffset, 0) }
                    .size(width = arrowSize * 2, height = arrowSize),
            )
        }

        // Tooltip on the end side: arrow on the edge facing the target. Row
        // ordering plus TooltipArrow's internal RTL handling mirror correctly.
        ResolvedPlacement.End -> Row(modifier = modifier) {
            TooltipArrow(
                placement = ResolvedPlacement.End,
                color = arrowColor,
                size = arrowSize,
                modifier = Modifier
                    .align(Alignment.Top)
                    .offset { IntOffset(0, alongEdgeOffset) }
                    .size(width = arrowSize, height = arrowSize * 2),
            )
            content()
        }

        ResolvedPlacement.Start -> Row(modifier = modifier) {
            content()
            TooltipArrow(
                placement = ResolvedPlacement.Start,
                color = arrowColor,
                size = arrowSize,
                modifier = Modifier
                    .align(Alignment.Top)
                    .offset { IntOffset(0, alongEdgeOffset) }
                    .size(width = arrowSize, height = arrowSize * 2),
            )
        }
    }
}
