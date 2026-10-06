package com.mohamedrejeb.waypoint.sample.trip

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.WaypointHost
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.sample.data.Trip
import com.mohamedrejeb.waypoint.sample.kit.AppBar
import com.mohamedrejeb.waypoint.sample.kit.KitButton
import com.mohamedrejeb.waypoint.sample.kit.KitButtonStyle
import com.mohamedrejeb.waypoint.sample.kit.KitIconButton
import com.mohamedrejeb.waypoint.sample.kit.ScreenColumn
import com.mohamedrejeb.waypoint.sample.kit.SectionTitle
import com.mohamedrejeb.waypoint.sample.tour.TripTarget
import com.mohamedrejeb.waypoint.sample.tour.TripTooltip
import com.mohamedrejeb.waypoint.sample.tour.tripHighlightStyle

private val SheetTargets = setOf(TripTarget.SheetName, TripTarget.SheetStay)

/**
 * A trip: its route map, its stops and the "Add stop" sheet. Hosts the tour
 * that shows canvas targets, several targets in one step, auto-scroll and a
 * step inside the sheet.
 */
@Composable
fun TripScreen(
    trip: Trip,
    ui: TripUiState,
    tour: WaypointState<TripTarget>,
    onBack: () -> Unit,
) {
    WaypointHost(
        state = tour,
        highlightStyle = tripHighlightStyle(),
        onTourComplete = { ui.sheetOpen = false },
        onTourCancel = { ui.sheetOpen = false },
        tooltipContent = { TripTooltip(it) },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppBar(
                title = trip.title,
                onBack = onBack,
                actions = {
                    if (!tour.isActive) {
                        KitButton(
                            text = "Tour",
                            style = KitButtonStyle.Soft,
                            icon = Icons.Rounded.PlayArrow,
                            onClick = {
                                ui.reset()
                                // Clears a previous completion so the tour can run again.
                                tour.resetCompletion()
                                tour.start()
                            },
                        )
                    }
                    KitIconButton(
                        icon = Icons.Rounded.IosShare,
                        contentDescription = "Share",
                        onClick = {},
                        modifier = Modifier.waypointTarget(tour, TripTarget.Share),
                    )
                },
            )
            ScreenColumn {
                RouteMap(stops = trip.stops, tour = tour)
                Spacer(Modifier.height(4.dp))
                SectionTitle("Stops")
                StopList(stops = trip.stops, addedStops = ui.addedStops, tour = tour)
                KitButton(
                    text = "Add stop",
                    icon = Icons.Rounded.Add,
                    onClick = { ui.sheetOpen = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .waypointTarget(tour, TripTarget.AddStop),
                )
            }
        }

        if (ui.sheetOpen) {
            AddStopSheet(
                ui = ui,
                tour = tour,
                onDismiss = { onSheetDismissed(ui, tour) },
            )
        }
    }
}

/**
 * The user closed the sheet themselves. If the tour was on a step inside it,
 * that step's target is gone for good, so the tour ends instead of waiting.
 */
internal fun onSheetDismissed(ui: TripUiState, tour: WaypointState<TripTarget>) {
    ui.sheetOpen = false
    if (tour.isActive && tour.currentStep?.targetKey in SheetTargets) {
        tour.stop()
    }
}
