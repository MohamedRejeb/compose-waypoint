package com.mohamedrejeb.waypoint.sample.newtrip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.WaypointHost
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.sample.kit.AppBar
import com.mohamedrejeb.waypoint.sample.kit.KitButton
import com.mohamedrejeb.waypoint.sample.kit.KitButtonStyle
import com.mohamedrejeb.waypoint.sample.kit.KitCard
import com.mohamedrejeb.waypoint.sample.kit.KitChip
import com.mohamedrejeb.waypoint.sample.kit.KitTextField
import com.mohamedrejeb.waypoint.sample.kit.ScreenColumn
import com.mohamedrejeb.waypoint.sample.kit.SectionTitle
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import com.mohamedrejeb.waypoint.sample.tour.NewTripTarget
import com.mohamedrejeb.waypoint.sample.tour.TripTooltip
import com.mohamedrejeb.waypoint.sample.tour.tripHighlightStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val RouteLoadingMillis = 1200L

/**
 * The New trip form, and the host of the hands-on tutorial. During the
 * tutorial the user types and taps inside the highlighted element while the
 * rest of the screen is blocked.
 */
@Composable
fun NewTripScreen(
    form: NewTripFormState,
    tour: WaypointState<NewTripTarget>,
    onBack: () -> Unit,
) {
    WaypointHost(
        state = tour,
        highlightStyle = tripHighlightStyle(),
        tooltipContent = { TripTooltip(it) },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppBar(
                title = "New trip",
                onBack = onBack,
                actions = {
                    if (!tour.isActive) {
                        KitButton(
                            text = "Tutorial",
                            style = KitButtonStyle.Soft,
                            icon = Icons.Rounded.PlayArrow,
                            onClick = {
                                form.reset()
                                // Clears a previous completion so the tour can run again.
                                tour.resetCompletion()
                                tour.start()
                            },
                        )
                    }
                },
            )
            ScreenColumn {
                NewTripForm(form = form, tour = tour)
            }
        }
    }
}

@Composable
private fun NewTripForm(
    form: NewTripFormState,
    tour: WaypointState<NewTripTarget>,
) {
    // While a step is pending (the route gate is running) nothing is
    // highlighted and nothing is blocked, so the form is disabled meanwhile.
    val enabled = !tour.isActive || tour.isStepVisible
    val scope = rememberCoroutineScope()

    KitTextField(
        value = form.name,
        onValueChange = { form.name = it },
        label = "Trip name",
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .waypointTarget(tour, NewTripTarget.Name),
    )
    KitTextField(
        value = form.destination,
        onValueChange = { form.destination = it },
        label = "Destination",
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .waypointTarget(tour, NewTripTarget.Destination),
    )
    Spacer(Modifier.height(4.dp))
    SectionTitle("Travel style")
    Row(
        modifier = Modifier.waypointTarget(tour, NewTripTarget.Style),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TravelStyle.entries.forEach { style ->
            KitChip(
                text = style.label,
                selected = form.style == style,
                enabled = enabled,
                onClick = { form.style = style },
            )
        }
    }
    Spacer(Modifier.height(4.dp))
    when {
        form.routeVisible -> RouteCard(
            form = form,
            modifier = Modifier
                .fillMaxWidth()
                .waypointTarget(tour, NewTripTarget.Route),
        )
        form.routeLoading -> RouteLoadingRow()
        else -> KitButton(
            text = "Find route",
            style = KitButtonStyle.Soft,
            enabled = enabled && form.isNameValid && form.isDestinationValid,
            onClick = {
                scope.launch {
                    form.routeLoading = true
                    delay(RouteLoadingMillis)
                    form.routeLoading = false
                    form.routeVisible = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
    KitButton(
        text = if (form.created) "Trip created" else "Create trip",
        enabled = enabled && !form.created,
        onClick = { form.created = true },
        modifier = Modifier
            .fillMaxWidth()
            .waypointTarget(tour, NewTripTarget.Create),
    )
}

@Composable
private fun RouteLoadingRow() {
    KitCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = SampleTheme.colors.accent,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Finding the best route",
                style = MaterialTheme.typography.bodyMedium,
                color = SampleTheme.colors.inkMuted,
            )
        }
    }
}

@Composable
private fun RouteCard(
    form: NewTripFormState,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    val stops = when (form.style) {
        TravelStyle.Relaxed -> 3
        TravelStyle.Packed -> 7
        else -> 5
    }
    KitCard(modifier = modifier, color = colors.surfaceTint) {
        Text(
            text = "Route found",
            style = MaterialTheme.typography.titleSmall,
            color = colors.ink,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${form.name.trim()} to ${form.destination.trim()}, $stops stops",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.inkMuted,
        )
    }
}
