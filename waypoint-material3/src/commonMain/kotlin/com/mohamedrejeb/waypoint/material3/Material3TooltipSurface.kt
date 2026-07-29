package com.mohamedrejeb.waypoint.material3

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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.LocalTooltipArrowGeometry
import com.mohamedrejeb.waypoint.core.ResolvedPlacement
import com.mohamedrejeb.waypoint.core.TooltipArrow
import kotlin.math.roundToInt

private val ArrowSize = 10.dp

/**
 * Lays out tooltip [content] together with an arrow pointing at the target,
 * positioned from [LocalTooltipArrowGeometry]. Renders bare content when no
 * geometry is available (content composed outside a tooltip popup) or when
 * [showArrow] is false.
 */
@Composable
internal fun Material3TooltipSurface(
    arrowColor: Color,
    showArrow: Boolean,
    content: @Composable () -> Unit,
) {
    val geometry = LocalTooltipArrowGeometry.current
    if (!showArrow || geometry == null) {
        content()
        return
    }

    val arrowSizePx = with(LocalDensity.current) { ArrowSize.toPx() }
    // The arrow composable is 2*ArrowSize wide along the tooltip edge, so
    // shift by one ArrowSize to center it on the geometry offset.
    val alongEdgeOffset = (geometry.arrowOffset - arrowSizePx).roundToInt()

    when (geometry.placement) {
        // Tooltip below the target: arrow sits on the top edge pointing up.
        ResolvedPlacement.Bottom -> Column {
            TooltipArrow(
                placement = ResolvedPlacement.Bottom,
                color = arrowColor,
                size = ArrowSize,
                modifier = Modifier
                    .align(AbsoluteAlignment.Left)
                    .offset { IntOffset(alongEdgeOffset, 0) }
                    .size(width = ArrowSize * 2, height = ArrowSize),
            )
            content()
        }

        // Tooltip above the target: arrow on the bottom edge pointing down.
        ResolvedPlacement.Top -> Column {
            content()
            TooltipArrow(
                placement = ResolvedPlacement.Top,
                color = arrowColor,
                size = ArrowSize,
                modifier = Modifier
                    .align(AbsoluteAlignment.Left)
                    .offset { IntOffset(alongEdgeOffset, 0) }
                    .size(width = ArrowSize * 2, height = ArrowSize),
            )
        }

        // Tooltip on the end side: arrow on the edge facing the target. Row
        // ordering plus TooltipArrow's internal RTL handling mirror correctly.
        ResolvedPlacement.End -> Row {
            TooltipArrow(
                placement = ResolvedPlacement.End,
                color = arrowColor,
                size = ArrowSize,
                modifier = Modifier
                    .align(Alignment.Top)
                    .offset { IntOffset(0, alongEdgeOffset) }
                    .size(width = ArrowSize, height = ArrowSize * 2),
            )
            content()
        }

        ResolvedPlacement.Start -> Row {
            content()
            TooltipArrow(
                placement = ResolvedPlacement.Start,
                color = arrowColor,
                size = ArrowSize,
                modifier = Modifier
                    .align(Alignment.Top)
                    .offset { IntOffset(0, alongEdgeOffset) }
                    .size(width = ArrowSize, height = ArrowSize * 2),
            )
        }
    }
}
