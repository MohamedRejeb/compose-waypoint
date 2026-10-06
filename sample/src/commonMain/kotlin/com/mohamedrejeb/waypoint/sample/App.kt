package com.mohamedrejeb.waypoint.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.mohamedrejeb.waypoint.core.WaypointSequenceEffect
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.sample.data.SampleTrips
import com.mohamedrejeb.waypoint.sample.lab.LabScreen
import com.mohamedrejeb.waypoint.sample.navigation.Route
import com.mohamedrejeb.waypoint.sample.navigation.routeOf
import com.mohamedrejeb.waypoint.sample.navigation.stopToursNotOn
import com.mohamedrejeb.waypoint.sample.newtrip.NewTripFormState
import com.mohamedrejeb.waypoint.sample.newtrip.NewTripScreen
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import com.mohamedrejeb.waypoint.sample.tour.Chapter
import com.mohamedrejeb.waypoint.sample.tour.SampleTours
import com.mohamedrejeb.waypoint.sample.tour.rememberSampleTours
import com.mohamedrejeb.waypoint.sample.trip.TripScreen
import com.mohamedrejeb.waypoint.sample.trip.TripUiState
import com.mohamedrejeb.waypoint.sample.trips.TripsScreen

/**
 * The sample app: a small trip planner whose onboarding is three Waypoint
 * tours chained into one sequence, plus a Lab for trying every option.
 *
 * The tour states live here, above the navigation, so the sequence can move
 * from one screen to the next. Each screen hosts the tour that runs on it.
 */
@Composable
fun App(autoStartTour: Boolean = true) {
    var dark by rememberSaveable { mutableStateOf(false) }

    SampleTheme(dark = dark) {
        val form = remember { NewTripFormState() }
        val tripUi = remember { TripUiState() }
        val tours = rememberSampleTours(form, tripUi)
        val backStack = remember { NavBackStack(Route.Trips as Route) }

        TourNavigationEffects(
            tours = tours,
            form = form,
            tripUi = tripUi,
            backStack = backStack,
            autoStartTour = autoStartTour,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SampleTheme.colors.background),
        ) {
            SampleNavDisplay(
                backStack = backStack,
                tours = tours,
                form = form,
                tripUi = tripUi,
                dark = dark,
                onToggleTheme = { dark = !dark },
            )
        }
    }
}

/**
 * Keeps the tours and the navigation in step: the sequence decides which
 * tour runs, the app shows the screen that hosts it, and a tour ends when
 * its screen is left.
 */
@Composable
private fun TourNavigationEffects(
    tours: SampleTours,
    form: NewTripFormState,
    tripUi: TripUiState,
    backStack: MutableList<Route>,
    autoStartTour: Boolean,
) {
    // Tour states are saved and come back running after process death, but
    // the screens they were running on (the form, the open sheet, the back
    // stack) are not saved here. A restored tour is ended rather than left
    // waiting on a screen that is no longer in the state it expects.
    LaunchedEffect(Unit) {
        tours.sequence.stop()
        tours.chapters.forEach { if (it.state.isActive) it.state.stop() }
    }

    // Starts the next chapter when one completes, stops when one is cancelled.
    WaypointSequenceEffect(tours.sequence)

    // The sequence only knows which tour is running. Showing the screen that
    // hosts it is the app's job. The tour is already active at this point,
    // which is fine because the first step of every chapter does not depend
    // on the state that prepare() resets.
    val sequenceTour = tours.sequence.currentTour
    LaunchedEffect(sequenceTour) {
        val tour = tours.sequence.currentTour ?: return@LaunchedEffect
        tours.prepare(tour, form, tripUi)
        backStack.show(tours.routeOf(tour))
    }

    // A tour belongs to the screen that hosts it: leaving that screen, for
    // example with the system back gesture, ends the tour.
    val currentRoute = backStack.lastOrNull()
    LaunchedEffect(currentRoute) {
        if (currentRoute != null) tours.stopToursNotOn(currentRoute)
        if (currentRoute !is Route.Trip) tripUi.sheetOpen = false
    }

    // First launch: the onboarding is offered once, after the first screen
    // has been drawn. NavDisplay does not pick up a state change made during
    // its very first frame.
    var onboardingOffered by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        if (autoStartTour && !onboardingOffered && !tours.lookAround.hasCompleted) {
            tours.sequence.start()
        }
        onboardingOffered = true
    }
}

@Composable
private fun SampleNavDisplay(
    backStack: NavBackStack<Route>,
    tours: SampleTours,
    form: NewTripFormState,
    tripUi: TripUiState,
    dark: Boolean,
    onToggleTheme: () -> Unit,
) {
    val goBack: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    val startChapter: (Chapter) -> Unit = { chapter ->
        tours.sequence.stop()
        tours.chapters.forEach { if (it.state.isActive) it.state.stop() }
        tours.prepare(chapter.state, form, tripUi)
        // Show the hosting screen first, then start the tour on it.
        backStack.show(tours.routeOf(chapter.state))
        // A completed tour does not start again until its completion is cleared.
        chapter.state.resetCompletion()
        chapter.state.start()
    }

    NavDisplay(
        backStack = backStack,
        onBack = goBack,
        entryProvider = entryProvider {
            entry<Route.Trips> {
                TripsScreen(
                    trips = SampleTrips,
                    tours = tours,
                    dark = dark,
                    onToggleTheme = onToggleTheme,
                    onOpenLab = { backStack.add(Route.Lab) },
                    onOpenTrip = { backStack.add(Route.Trip(it.id)) },
                    onNewTrip = {
                        form.reset()
                        backStack.add(Route.NewTrip)
                    },
                    onStartChapter = startChapter,
                    onContinue = { tours.sequence.start() },
                    onExploreMap = { startChapter(tours.chapters.last()) },
                )
            }
            entry<Route.NewTrip> {
                NewTripScreen(form = form, tour = tours.planTrip, onBack = goBack)
            }
            entry<Route.Trip> { route ->
                TripScreen(
                    trip = SampleTrips.firstOrNull { it.id == route.id } ?: SampleTrips.first(),
                    ui = tripUi,
                    tour = tours.knowTrip,
                    onBack = goBack,
                )
            }
            entry<Route.Lab> {
                LabScreen(onBack = goBack)
            }
        },
    )
}

/** Puts the screen state a chapter works on back to its starting point. */
private fun SampleTours.prepare(
    tour: WaypointState<*>,
    form: NewTripFormState,
    tripUi: TripUiState,
) {
    when (tour) {
        planTrip -> form.reset()
        knowTrip -> tripUi.reset()
    }
}

/** Makes [route] the visible screen, on top of the Trips screen. */
private fun MutableList<Route>.show(route: Route) {
    if (lastOrNull() == route) return
    while (size > 1) removeAt(lastIndex)
    if (route != Route.Trips) add(route)
}
