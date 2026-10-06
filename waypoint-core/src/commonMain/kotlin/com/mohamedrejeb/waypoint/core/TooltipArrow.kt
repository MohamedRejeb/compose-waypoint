package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Draws a triangular arrow pointing toward the target element.
 *
 * The triangle is centered inside the composable's own bounds, so size the
 * modifier to [arrowWidth] along the tooltip edge and [size] across it (e.g.
 * `Modifier.size(width = 20.dp, height = 10.dp)` for [ResolvedPlacement.Top]/
 * [ResolvedPlacement.Bottom]) and position it using
 * [LocalTooltipArrowGeometry]. Layout direction is handled internally for
 * [ResolvedPlacement.Start]/[ResolvedPlacement.End]. [TooltipArrowBox] does
 * all of this for you.
 *
 * @param placement the tooltip's resolved placement; the arrow points from the
 *   tooltip toward the target (e.g. Bottom placement draws an upward arrow)
 * @param color arrow fill color, typically the tooltip background color
 * @param modifier modifier sizing and positioning the arrow
 * @param size distance the arrow protrudes from the tooltip edge
 * @param arrowWidth length of the arrow's base along the tooltip edge
 */
@Composable
public fun TooltipArrow(
    placement: ResolvedPlacement,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 10.dp,
    arrowWidth: Dp = size * 2,
) {
    val layoutDirection = LocalLayoutDirection.current

    Canvas(modifier = modifier) {
        val arrowSizePx = size.toPx()
        val halfBasePx = arrowWidth.toPx() / 2f
        val path = Path()

        when (placement) {
            // Arrow points UP (tooltip is below target), no RTL change
            ResolvedPlacement.Bottom -> {
                path.moveTo(this.size.width / 2f - halfBasePx, arrowSizePx)
                path.lineTo(this.size.width / 2f, 0f)
                path.lineTo(this.size.width / 2f + halfBasePx, arrowSizePx)
            }

            // Arrow points DOWN (tooltip is above target), no RTL change
            ResolvedPlacement.Top -> {
                path.moveTo(this.size.width / 2f - halfBasePx, 0f)
                path.lineTo(this.size.width / 2f, arrowSizePx)
                path.lineTo(this.size.width / 2f + halfBasePx, 0f)
            }

            // Tooltip is at End of target
            ResolvedPlacement.End -> {
                val pointsLeft = layoutDirection == LayoutDirection.Ltr
                if (pointsLeft) {
                    // Arrow points LEFT (target is to the left)
                    path.moveTo(arrowSizePx, this.size.height / 2f - halfBasePx)
                    path.lineTo(0f, this.size.height / 2f)
                    path.lineTo(arrowSizePx, this.size.height / 2f + halfBasePx)
                } else {
                    // Arrow points RIGHT (target is to the right in RTL)
                    path.moveTo(0f, this.size.height / 2f - halfBasePx)
                    path.lineTo(arrowSizePx, this.size.height / 2f)
                    path.lineTo(0f, this.size.height / 2f + halfBasePx)
                }
            }

            // Tooltip is at Start of target
            ResolvedPlacement.Start -> {
                val pointsRight = layoutDirection == LayoutDirection.Ltr
                if (pointsRight) {
                    // Arrow points RIGHT (target is to the right)
                    path.moveTo(0f, this.size.height / 2f - halfBasePx)
                    path.lineTo(arrowSizePx, this.size.height / 2f)
                    path.lineTo(0f, this.size.height / 2f + halfBasePx)
                } else {
                    // Arrow points LEFT (target is to the left in RTL)
                    path.moveTo(arrowSizePx, this.size.height / 2f - halfBasePx)
                    path.lineTo(0f, this.size.height / 2f)
                    path.lineTo(arrowSizePx, this.size.height / 2f + halfBasePx)
                }
            }
        }

        path.close()
        drawPath(path = path, color = color, style = Fill)
    }
}
