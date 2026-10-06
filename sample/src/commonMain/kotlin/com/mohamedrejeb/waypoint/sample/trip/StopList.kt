package com.mohamedrejeb.waypoint.sample.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.sample.data.Stop
import com.mohamedrejeb.waypoint.sample.kit.ListRow
import com.mohamedrejeb.waypoint.sample.theme.Dimens
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import com.mohamedrejeb.waypoint.sample.tour.TripTarget

/**
 * The trip's stops, followed by the ones added from the sheet. The featured
 * stop and a stop far down the list are tour targets.
 */
/**
 * Index of the stop the auto-scroll step points at: far enough down a long
 * trip to start below the fold. A shorter trip uses its last stop.
 */
private const val FarStopIndex = 8

@Composable
fun StopList(
    stops: List<Stop>,
    addedStops: List<Stop>,
    tour: WaypointState<TripTarget>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val farStopIndex = minOf(FarStopIndex, stops.lastIndex)
        stops.forEachIndexed { index, stop ->
            StopRow(
                number = index + 1,
                stop = stop,
                modifier = when (index) {
                    FeaturedStopIndex -> Modifier.waypointTarget(tour, TripTarget.StopRow)
                    farStopIndex -> Modifier.waypointTarget(tour, TripTarget.FarStop)
                    else -> Modifier
                },
            )
        }
        addedStops.forEachIndexed { index, stop ->
            StopRow(number = stops.size + index + 1, stop = stop)
        }
    }
}

@Composable
private fun StopRow(
    number: Int,
    stop: Stop,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    ListRow(
        title = stop.name,
        subtitle = stop.stay,
        leading = {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.ink),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.background,
                )
            }
        },
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusRow))
            .background(colors.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    )
}
