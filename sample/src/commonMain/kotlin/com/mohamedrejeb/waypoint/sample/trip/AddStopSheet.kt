package com.mohamedrejeb.waypoint.sample.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.WaypointOverlayHost
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.waypointTarget
import com.mohamedrejeb.waypoint.sample.kit.KitButton
import com.mohamedrejeb.waypoint.sample.kit.KitSheet
import com.mohamedrejeb.waypoint.sample.kit.SheetHandle
import com.mohamedrejeb.waypoint.sample.kit.KitTextField
import com.mohamedrejeb.waypoint.sample.kit.SegmentedControl
import com.mohamedrejeb.waypoint.sample.theme.Dimens
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import com.mohamedrejeb.waypoint.sample.tour.TripTarget
import com.mohamedrejeb.waypoint.sample.tour.TripTooltip
import com.mohamedrejeb.waypoint.sample.tour.tripHighlightStyle

/**
 * The "Add stop" sheet. A sheet is a separate window, so the screen's host
 * cannot see into it: the WaypointOverlayHost here shares the tour's state
 * and renders the steps whose targets live inside the sheet. It wraps the
 * whole sheet content, grab bar included, so its dim covers all of it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStopSheet(
    ui: TripUiState,
    tour: WaypointState<TripTarget>,
    onDismiss: () -> Unit,
) {
    val colors = SampleTheme.colors
    KitSheet(
        onDismiss = onDismiss,
        // During the tour the screen behind the sheet is dimmed like the
        // sheet itself, so the two read as one dimmed surface.
        scrimColor = if (tour.isActive) {
            colors.scrim.copy(alpha = colors.scrimAlpha)
        } else {
            BottomSheetDefaults.ScrimColor
        },
    ) {
        WaypointOverlayHost(
            state = tour,
            highlightStyle = tripHighlightStyle(),
            tooltipContent = { TripTooltip(it) },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenPadding)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SheetHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
                Text(
                    text = "New stop",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.ink,
                )
                KitTextField(
                    value = ui.newStopName,
                    onValueChange = { ui.newStopName = it },
                    label = "Stop name",
                    modifier = Modifier
                        .fillMaxWidth()
                        .waypointTarget(tour, TripTarget.SheetName),
                )
                SegmentedControl(
                    options = StayOptions,
                    selected = ui.stay,
                    onSelect = { ui.stay = it },
                    label = { it },
                    modifier = Modifier.waypointTarget(tour, TripTarget.SheetStay),
                )
                KitButton(
                    text = "Save stop",
                    enabled = ui.newStopName.isNotBlank(),
                    onClick = ui::addStop,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
