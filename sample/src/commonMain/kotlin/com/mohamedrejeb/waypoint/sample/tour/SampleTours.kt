package com.mohamedrejeb.waypoint.sample.tour

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.WaypointHintState
import com.mohamedrejeb.waypoint.core.WaypointSequenceState
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.rememberWaypointHintState
import com.mohamedrejeb.waypoint.core.rememberWaypointSequenceState
import com.mohamedrejeb.waypoint.sample.newtrip.NewTripFormState
import com.mohamedrejeb.waypoint.sample.trip.TripUiState

/** One entry of the "Getting started" checklist. */
@Immutable
data class Chapter(
    val title: String,
    val subtitle: String,
    val state: WaypointState<*>,
)

/**
 * Every tour of the app. The states are created above the navigation so the
 * sequence can run across screens; each screen hosts its own chapter.
 */
@Stable
class SampleTours(
    val lookAround: WaypointState<TripsTarget>,
    val planTrip: WaypointState<NewTripTarget>,
    val knowTrip: WaypointState<TripTarget>,
    val sequence: WaypointSequenceState,
    val hints: WaypointHintState<TripsHint>,
    val persistence: InMemoryPersistence,
) {
    val chapters: List<Chapter> = listOf(
        Chapter("Look around", "A quick tour of your trips", lookAround),
        Chapter("Plan a trip", "Hands on, you fill in the form", planTrip),
        Chapter("Know your trip", "The map, the stops and the sheet", knowTrip),
    )

    /** Forgets completed chapters and dismissed hints. */
    fun resetAll() {
        sequence.reset()
        hints.resetAll()
    }
}

@Composable
fun rememberSampleTours(
    form: NewTripFormState,
    tripUi: TripUiState,
): SampleTours {
    // Saved like the tour states are, so progress survives with them.
    val persistence = rememberSaveable(
        saver = listSaver(
            save = { it.completedIds.toList() },
            restore = { InMemoryPersistence(it.toSet()) },
        ),
    ) { InMemoryPersistence() }
    val lookAround = rememberLookAroundTour(persistence)
    val planTrip = rememberPlanTripTour(form, persistence)
    val knowTrip = rememberKnowTripTour(tripUi, persistence)
    val sequence = rememberWaypointSequenceState(lookAround, planTrip, knowTrip)
    val hints = rememberWaypointHintState<TripsHint>(
        persistence = persistence,
        groupId = "trips-hints",
    ) {
        hint(TripsHint.Search) {
            title = "Search trips"
            description = "Find a trip by name or by stop."
            // Below the button. Auto would pick the side with the most room,
            // which on a wide window is beside it, over the next action.
            placement = TooltipPlacement.Bottom
        }
        hint(TripsHint.Filter) {
            title = "Filter"
            description = "Show only upcoming trips."
            placement = TooltipPlacement.Bottom
        }
    }
    return remember(lookAround, planTrip, knowTrip, sequence, hints, persistence) {
        SampleTours(lookAround, planTrip, knowTrip, sequence, hints, persistence)
    }
}
