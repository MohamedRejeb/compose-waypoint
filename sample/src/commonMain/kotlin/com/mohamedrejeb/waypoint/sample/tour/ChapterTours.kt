package com.mohamedrejeb.waypoint.sample.tour

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import com.mohamedrejeb.waypoint.core.StepBuilder
import com.mohamedrejeb.waypoint.core.TargetInteraction
import com.mohamedrejeb.waypoint.core.TooltipPlacement
import com.mohamedrejeb.waypoint.core.WaypointPersistence
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.rememberWaypointState
import com.mohamedrejeb.waypoint.sample.newtrip.NewTripFormState
import com.mohamedrejeb.waypoint.sample.newtrip.TravelStyle
import com.mohamedrejeb.waypoint.sample.trip.TripUiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first

private const val RouteLoadingMillis = 1200L
private const val TypingPauseMillis = 900L

/**
 * Returns once the user has edited the text, stopped typing for a moment,
 * and left it valid. Advancing on the first valid character would pull the
 * user out of the field in the middle of a word.
 *
 * The value the step starts with is skipped (drop), so coming back to a field
 * that is already filled in does not bounce straight to the next step. For
 * that case the tooltip offers Next instead, see [canContinue].
 */
@OptIn(FlowPreview::class)
private suspend fun awaitTypingPause(read: () -> String, isValid: () -> Boolean) {
    snapshotFlow(read)
        .drop(1)
        .debounce(TypingPauseMillis)
        .first { isValid() }
}

/**
 * The tooltip of a step the user completes by acting. Next stays hidden
 * until the step's own condition holds, so the action cannot be skipped, and
 * it is there when the user returns to a step that is already done.
 */
private fun <K> StepBuilder<K>.canContinue(isDone: () -> Boolean) {
    content { scope ->
        TripTooltip(scope, showNext = !scope.advancesAutomatically || isDone())
    }
}

/** Chapter 1: a classic spotlight tour of the Trips screen. */
@Composable
internal fun rememberLookAroundTour(
    persistence: WaypointPersistence,
): WaypointState<TripsTarget> = rememberWaypointState(
    tourId = "look-around",
    persistence = persistence,
) {
    step {
        title = "Welcome to Trips"
        description = "A one-minute look around. Use Next, the arrow keys, " +
            "or tap outside the tooltip."
    }
    step(TripsTarget.Search) {
        title = "Find a trip"
        description = "Search by trip name or by stop."
        placement = TooltipPlacement.Bottom
    }
    step(TripsTarget.FirstTrip) {
        title = "Your trips"
        description = "Each card opens the route map and the list of stops."
    }
    step(TripsTarget.NewTrip) {
        title = "Start a new one"
        description = "Planning begins here. The next chapter walks you through it."
        placement = TooltipPlacement.Top
    }
    step(TripsTarget.ThemeSwitch) {
        title = "Light or dark"
        description = "Go on, flip it. The tour keeps running."
        placement = TooltipPlacement.Bottom
        // The switch stays usable while everything else is blocked.
        interaction = TargetInteraction.PassThrough
    }
    step(TripsTarget.Checklist) {
        title = "Pick up where you left off"
        description = "Your progress lives on this card. Continue starts the next chapter."
    }
}

/**
 * Chapter 2: a hands-on tutorial. The user works inside the highlighted
 * element (PassThrough) and each step advances from what they do (advanceOn).
 */
@Composable
internal fun rememberPlanTripTour(
    form: NewTripFormState,
    persistence: WaypointPersistence,
): WaypointState<NewTripTarget> = rememberWaypointState(
    tourId = "plan-trip",
    persistence = persistence,
) {
    step {
        title = "Plan a trip, hands on"
        description = "You fill in this form yourself. Only the highlighted " +
            "part of the screen responds at each step."
    }
    step(NewTripTarget.Name) {
        title = "Name your trip"
        description = "Type a name. The tour moves on when you stop typing."
        interaction = TargetInteraction.PassThrough
        advanceOn { awaitTypingPause(read = { form.name }, isValid = { form.isNameValid }) }
        canContinue { form.isNameValid }
    }
    step(NewTripTarget.Destination) {
        title = "Where to?"
        description = "Type a destination, then pause."
        interaction = TargetInteraction.PassThrough
        advanceOn { awaitTypingPause(read = { form.destination }, isValid = { form.isDestinationValid }) }
        canContinue { form.isDestinationValid }
    }
    step(NewTripTarget.Style) {
        title = "Pick a pace"
        description = "Choose any travel style."
        interaction = TargetInteraction.PassThrough
        // A new choice advances, the one already made when the step starts does not.
        advanceOn { snapshotFlow { form.style }.drop(1).first { it != null } }
        canContinue { form.style != null }
    }
    step(NewTripTarget.Route) {
        title = "Your route"
        description = "This step waited for the route to load before showing. " +
            "With the Relaxed pace, the next step is skipped."
        // The gate holds the step until the route card exists.
        beforeShow {
            // Already loaded when the user comes back to this step.
            if (form.routeVisible) return@beforeShow
            form.routeLoading = true
            delay(RouteLoadingMillis)
            form.routeLoading = false
            form.routeVisible = true
        }
    }
    step(NewTripTarget.Create) {
        title = "Create it"
        description = "Tap the highlighted button."
        interaction = TargetInteraction.PassThrough
        showIf { form.style != TravelStyle.Relaxed }
        advanceOn { snapshotFlow { form.created }.drop(1).first { it } }
        canContinue { form.created }
    }
    step {
        title = "That's a trip"
        description = "Every step advanced from something you did."
    }
}

/**
 * Chapter 3: canvas targets, several targets in one step, auto-scroll, and a
 * tour that crosses into a bottom sheet.
 */
@Composable
internal fun rememberKnowTripTour(
    tripUi: TripUiState,
    persistence: WaypointPersistence,
): WaypointState<TripTarget> = rememberWaypointState(
    tourId = "know-trip",
    persistence = persistence,
) {
    step(TripTarget.MapStop) {
        title = "Stops on the map"
        description = "This marker is drawn on a Canvas. Its row in the list " +
            "is highlighted with it."
        additionalTargets = listOf(TripTarget.StopRow)
    }
    step(TripTarget.FarStop) {
        title = "Further down the route"
        description = "When a stop is off screen, the tour scrolls it to the middle."
        placement = TooltipPlacement.Top
    }
    step(TripTarget.AddStop) {
        title = "Add a stop"
        description = "Tap the highlighted button to open the sheet."
        placement = TooltipPlacement.Top
        interaction = TargetInteraction.ClickToAdvance
        onEnter { tripUi.sheetOpen = false }
    }
    step(TripTarget.SheetName) {
        title = "Name the stop"
        description = "The tour followed you into the sheet."
        beforeShow { tripUi.sheetOpen = true }
    }
    step(TripTarget.SheetStay) {
        title = "How long?"
        description = "Pick how long you will stay."
        beforeShow { tripUi.sheetOpen = true }
    }
    step(TripTarget.Share) {
        placement = TooltipPlacement.Bottom
        beforeShow { tripUi.sheetOpen = false }
        // This step draws its own card instead of the shared tooltip.
        content { scope -> ShareStepCard(scope) }
    }
}
