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
 */
@Composable
internal fun TooltipArrow(
    placement: ResolvedPlacement,
    color: Color,
    size: Dp = 10.dp,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current

    Canvas(modifier = modifier) {
        val arrowSizePx = size.toPx()
        val path = Path()

        when (placement) {
            // Arrow points UP (tooltip is below target) — no RTL change
            ResolvedPlacement.Bottom -> {
                path.moveTo(this.size.width / 2f - arrowSizePx, arrowSizePx)
                path.lineTo(this.size.width / 2f, 0f)
                path.lineTo(this.size.width / 2f + arrowSizePx, arrowSizePx)
            }

            // Arrow points DOWN (tooltip is above target) — no RTL change
            ResolvedPlacement.Top -> {
                path.moveTo(this.size.width / 2f - arrowSizePx, 0f)
                path.lineTo(this.size.width / 2f, arrowSizePx)
                path.lineTo(this.size.width / 2f + arrowSizePx, 0f)
            }

            // Tooltip is at End of target
            ResolvedPlacement.End -> {
                val pointsLeft = layoutDirection == LayoutDirection.Ltr
                if (pointsLeft) {
                    // Arrow points LEFT (target is to the left)
                    path.moveTo(arrowSizePx, this.size.height / 2f - arrowSizePx)
                    path.lineTo(0f, this.size.height / 2f)
                    path.lineTo(arrowSizePx, this.size.height / 2f + arrowSizePx)
                } else {
                    // Arrow points RIGHT (target is to the right in RTL)
                    path.moveTo(0f, this.size.height / 2f - arrowSizePx)
                    path.lineTo(arrowSizePx, this.size.height / 2f)
                    path.lineTo(0f, this.size.height / 2f + arrowSizePx)
                }
            }

            // Tooltip is at Start of target
            ResolvedPlacement.Start -> {
                val pointsRight = layoutDirection == LayoutDirection.Ltr
                if (pointsRight) {
                    // Arrow points RIGHT (target is to the right)
                    path.moveTo(0f, this.size.height / 2f - arrowSizePx)
                    path.lineTo(arrowSizePx, this.size.height / 2f)
                    path.lineTo(0f, this.size.height / 2f + arrowSizePx)
                } else {
                    // Arrow points LEFT (target is to the left in RTL)
                    path.moveTo(arrowSizePx, this.size.height / 2f - arrowSizePx)
                    path.lineTo(0f, this.size.height / 2f)
                    path.lineTo(arrowSizePx, this.size.height / 2f + arrowSizePx)
                }
            }
        }

        path.close()
        drawPath(path = path, color = color, style = Fill)
    }
}
