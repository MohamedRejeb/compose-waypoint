package com.mohamedrejeb.waypoint.sample.trip

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.waypoint.core.ExperimentalWaypointApi
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.waypointCanvasTarget
import com.mohamedrejeb.waypoint.sample.data.Stop
import com.mohamedrejeb.waypoint.sample.theme.Dimens
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import com.mohamedrejeb.waypoint.sample.tour.TripTarget

private val MapHeight = 180.dp
private val MarkerRadius = 8.dp
private val MarkerStroke = 3.dp
private val RouteStroke = 5.dp

/** Index of the stop the tour points at, on the map and in the list. */
internal const val FeaturedStopIndex = 1

/**
 * A stylised route map drawn on one Canvas. The markers are not composables,
 * so the tour targets one through waypointCanvasTarget, which takes the
 * marker's bounds in the Canvas's own coordinates.
 */
@OptIn(ExperimentalWaypointApi::class)
@Composable
fun RouteMap(
    stops: List<Stop>,
    tour: WaypointState<TripTarget>,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    val markerRadiusPx = with(LocalDensity.current) { MarkerRadius.toPx() }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MapHeight)
            .clip(RoundedCornerShape(Dimens.RadiusLarge))
            .background(colors.surfaceTint),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it.toSize() }
                .waypointCanvasTarget(tour, TripTarget.MapStop) {
                    val center = stops[FeaturedStopIndex].positionIn(canvasSize)
                    Rect(center = center, radius = markerRadiusPx)
                },
        ) {
            val points = stops.map { it.positionIn(size) }
            drawRoute(points = points, color = colors.accent)
            points.forEachIndexed { index, point ->
                drawMarker(
                    center = point,
                    featured = index == FeaturedStopIndex,
                    ink = colors.ink,
                    fill = colors.background,
                )
            }
        }
    }
}

private fun Stop.positionIn(size: Size): Offset = Offset(x * size.width, y * size.height)

private fun DrawScope.drawRoute(points: List<Offset>, color: Color) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.zipWithNext { from, to ->
            val midX = (from.x + to.x) / 2f
            cubicTo(midX, from.y, midX, to.y, to.x, to.y)
        }
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = RouteStroke.toPx(), cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawMarker(center: Offset, featured: Boolean, ink: Color, fill: Color) {
    val radius = MarkerRadius.toPx()
    drawCircle(color = if (featured) ink else fill, radius = radius, center = center)
    drawCircle(
        color = ink,
        radius = radius,
        center = center,
        style = Stroke(width = MarkerStroke.toPx()),
    )
}
