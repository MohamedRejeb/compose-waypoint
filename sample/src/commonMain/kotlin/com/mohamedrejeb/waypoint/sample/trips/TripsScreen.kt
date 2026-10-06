package com.mohamedrejeb.waypoint.sample.trips

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.BeaconStyle
import com.mohamedrejeb.waypoint.core.OverlayClickBehavior
import com.mohamedrejeb.waypoint.core.WaypointBeacon
import com.mohamedrejeb.waypoint.core.WaypointHint
import com.mohamedrejeb.waypoint.core.WaypointHost
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.sample.data.Trip
import com.mohamedrejeb.waypoint.sample.kit.AppBar
import com.mohamedrejeb.waypoint.sample.kit.KitButton
import com.mohamedrejeb.waypoint.sample.kit.KitIconButton
import com.mohamedrejeb.waypoint.sample.kit.ScreenColumn
import com.mohamedrejeb.waypoint.sample.kit.SectionTitle
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import com.mohamedrejeb.waypoint.sample.tour.Chapter
import com.mohamedrejeb.waypoint.sample.tour.SampleTours
import com.mohamedrejeb.waypoint.sample.tour.TripHintTooltip
import com.mohamedrejeb.waypoint.sample.tour.TripTooltip
import com.mohamedrejeb.waypoint.sample.tour.TripsHint
import com.mohamedrejeb.waypoint.sample.tour.TripsTarget
import com.mohamedrejeb.waypoint.sample.tour.tripHighlightStyle

internal const val MapBeaconTag = "map-beacon"

/**
 * Home: the onboarding checklist and the list of trips. Hosts chapter 1, a
 * classic spotlight tour, and shows the two standalone features: hints on the
 * app bar actions and a beacon on the first trip.
 */
@Composable
fun TripsScreen(
    trips: List<Trip>,
    tours: SampleTours,
    dark: Boolean,
    onToggleTheme: () -> Unit,
    onOpenLab: () -> Unit,
    onOpenTrip: (Trip) -> Unit,
    onNewTrip: () -> Unit,
    onStartChapter: (Chapter) -> Unit,
    onContinue: () -> Unit,
    onExploreMap: () -> Unit,
) {
    val tour = tours.lookAround

    WaypointHost(
        state = tour,
        highlightStyle = tripHighlightStyle(),
        // Tapping anywhere outside the highlight moves to the next step.
        overlayClickBehavior = OverlayClickBehavior.NextStep,
        tooltipContent = { TripTooltip(it) },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppBar(
                title = "Trips",
                largeTitle = true,
                actions = {
                    TripsActions(
                        tours = tours,
                        dark = dark,
                        onToggleTheme = onToggleTheme,
                        onOpenLab = onOpenLab,
                    )
                },
            )
            ScreenColumn {
                GettingStartedCard(
                    tours = tours,
                    onStartChapter = onStartChapter,
                    onContinue = onContinue,
                    onReset = tours::resetAll,
                    modifier = Modifier
                        .fillMaxWidth()
                        .waypointTarget(tour, TripsTarget.Checklist),
                )
                Spacer(Modifier.height(8.dp))
                SectionTitle("Your trips")
                trips.forEachIndexed { index, trip ->
                    if (index == 0) {
                        FirstTripCard(
                            trip = trip,
                            tours = tours,
                            onClick = { onOpenTrip(trip) },
                            onExploreMap = onExploreMap,
                        )
                    } else {
                        TripCard(
                            trip = trip,
                            onClick = { onOpenTrip(trip) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                KitButton(
                    text = "New trip",
                    icon = Icons.Rounded.Add,
                    onClick = onNewTrip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .waypointTarget(tour, TripsTarget.NewTrip),
                )
            }
        }
    }
}

@Composable
private fun TripsActions(
    tours: SampleTours,
    dark: Boolean,
    onToggleTheme: () -> Unit,
    onOpenLab: () -> Unit,
) {
    val tour = tours.lookAround
    HintedAction(tours = tours, hint = TripsHint.Search) {
        KitIconButton(
            icon = Icons.Rounded.Search,
            contentDescription = "Search",
            onClick = {},
            modifier = Modifier.waypointTarget(tour, TripsTarget.Search),
        )
    }
    HintedAction(tours = tours, hint = TripsHint.Filter) {
        KitIconButton(
            icon = Icons.Rounded.Tune,
            contentDescription = "Filter",
            onClick = {},
        )
    }
    KitIconButton(
        icon = if (dark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
        contentDescription = if (dark) "Switch to light theme" else "Switch to dark theme",
        onClick = onToggleTheme,
        modifier = Modifier.waypointTarget(tour, TripsTarget.ThemeSwitch),
    )
    KitIconButton(
        icon = Icons.Rounded.Science,
        contentDescription = "Open lab",
        onClick = onOpenLab,
    )
}

/**
 * An app bar action with a standalone hint: a beacon the user can tap to
 * read about the action and dismiss for good. Hints step aside during the tour.
 */
@Composable
private fun HintedAction(
    tours: SampleTours,
    hint: TripsHint,
    content: @Composable () -> Unit,
) {
    if (tours.lookAround.isActive) {
        content()
    } else {
        WaypointHint(
            state = tours.hints,
            key = hint,
            tooltipContent = { TripHintTooltip(it) },
            content = content,
        )
    }
}

/**
 * The first trip carries a beacon until its tour has been taken. Tapping the
 * beacon opens the trip and starts that tour.
 */
@Composable
private fun FirstTripCard(
    trip: Trip,
    tours: SampleTours,
    onClick: () -> Unit,
    onExploreMap: () -> Unit,
) {
    val anyTourRunning = tours.chapters.any { it.state.isActive }
    WaypointBeacon(
        visible = !tours.knowTrip.hasCompleted && !anyTourRunning,
        style = BeaconStyle.Pulse(color = SampleTheme.colors.accent),
        alignment = Alignment.TopEnd,
        offset = DpOffset(x = (-6).dp, y = 6.dp),
        onClick = onExploreMap,
        modifier = Modifier.testTag(MapBeaconTag),
    ) {
        TripCard(
            trip = trip,
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .waypointTarget(tours.lookAround, TripsTarget.FirstTrip),
        )
    }
}
