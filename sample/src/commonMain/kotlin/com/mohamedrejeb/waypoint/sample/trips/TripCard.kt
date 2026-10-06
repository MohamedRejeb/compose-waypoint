package com.mohamedrejeb.waypoint.sample.trips

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.sample.data.Stop
import com.mohamedrejeb.waypoint.sample.data.Trip
import com.mohamedrejeb.waypoint.sample.kit.KitCard
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme

/** A trip in the list: title, dates and a miniature of its route. */
@Composable
fun TripCard(
    trip: Trip,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    KitCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${trip.dates}, ${trip.stops.size} stops",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
            MiniRoute(stops = trip.stops)
        }
    }
}

@Composable
private fun MiniRoute(stops: List<Stop>) {
    val colors = SampleTheme.colors
    Canvas(modifier = Modifier.size(width = 84.dp, height = 44.dp)) {
        val inset = 5.dp.toPx()
        val points = stops.map {
            Offset(
                x = inset + it.x * (size.width - 2 * inset),
                y = inset + it.y * (size.height - 2 * inset),
            )
        }
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.zipWithNext { from, to ->
                val midX = (from.x + to.x) / 2f
                cubicTo(midX, from.y, midX, to.y, to.x, to.y)
            }
        }
        drawPath(path, colors.accent, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        listOf(points.first(), points.last()).forEach { point ->
            drawCircle(colors.ink, radius = 4.dp.toPx(), center = point)
            drawCircle(colors.surface, radius = 2.dp.toPx(), center = point)
        }
    }
}
