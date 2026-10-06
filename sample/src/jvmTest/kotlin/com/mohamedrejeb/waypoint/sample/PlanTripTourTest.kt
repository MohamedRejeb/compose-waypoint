package com.mohamedrejeb.waypoint.sample

import androidx.compose.runtime.remember
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.mohamedrejeb.waypoint.sample.newtrip.NewTripFormState
import com.mohamedrejeb.waypoint.sample.newtrip.NewTripScreen
import com.mohamedrejeb.waypoint.core.WaypointSequenceEffect
import com.mohamedrejeb.waypoint.sample.navigation.Route
import com.mohamedrejeb.waypoint.sample.navigation.stopToursNotOn
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import com.mohamedrejeb.waypoint.sample.tour.SampleTours
import com.mohamedrejeb.waypoint.sample.tour.rememberSampleTours
import com.mohamedrejeb.waypoint.sample.trip.TripUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Drives chapter 2 the way a user would: real clicks through the root (so the
 * touch blocking is exercised) and typing into the highlighted fields.
 */
@OptIn(ExperimentalTestApi::class)
class PlanTripTourTest {

    private fun runTourTest(block: ComposeUiTest.() -> Unit) = runComposeUiTest {
        setContent {
            SampleTheme {
                val form = remember { NewTripFormState() }
                val tripUi = remember { TripUiState() }
                val tours = rememberSampleTours(form, tripUi)
                NewTripScreen(form = form, tour = tours.planTrip, onBack = {})
            }
        }
        block()
    }

    private fun ComposeUiTest.startAndReachNameStep() {
        tap("Tutorial")
        assertShown("Plan a trip, hands on")
        tap("Next")
        assertShown("Name your trip")
    }

    private fun ComposeUiTest.fillUpToStyle() {
        startAndReachNameStep()
        // A step that advances from the user's action offers no Next.
        assertNotShown("Next")
        field("Trip name").performClick()
        field("Trip name").performTextInput("Li")
        assertShown("Where to?")
        field("Destination").performClick()
        field("Destination").performTextInput("Porto")
        assertShown("Pick a pace")
    }

    @Test
    fun advancesFromInput() = runTourTest {
        fillUpToStyle()
        tap("Balanced")
        // The route step shows only after its beforeShow gate has finished.
        assertShown("Your route")
        assertShown("Route found")
        tap("Next")
        assertShown("Create it")
        tap("Create trip")
        assertShown("That's a trip")
        tap("Done")
        assertNotShown("That's a trip")
    }

    @Test
    fun relaxedSkipsCreate() = runTourTest {
        fillUpToStyle()
        tap("Relaxed")
        assertShown("Your route")
        tap("Next")
        assertShown("That's a trip")
        assertNotShown("Create it")
    }

    @Test
    fun outsideIsBlocked() = runTourTest {
        startAndReachNameStep()
        tap("Create trip")
        assertNotShown("Trip created")
    }

    @Test
    fun fastTypingAdvancesOnce() = runTourTest {
        startAndReachNameStep()
        field("Trip name").performClick()
        field("Trip name").performTextInput("Lisbon to Porto")
        assertShown("Where to?")
        assertNotShown("Pick a pace")
    }

    /** The text steps wait for a pause in typing, they do not jump ahead mid-word. */
    @Test
    fun typingStepWaitsForAPause() = runTourTest {
        startAndReachNameStep()
        field("Trip name").performClick()
        mainClock.autoAdvance = false

        field("Trip name").performTextInput("Li")
        mainClock.advanceTimeBy(400)
        assertEquals(0, onAllNodesWithText("Where to?").fetchSemanticsNodes().size, "advanced mid-word")

        // Typing again restarts the wait.
        field("Trip name").performTextInput("sbon")
        mainClock.advanceTimeBy(700)
        assertEquals(0, onAllNodesWithText("Where to?").fetchSemanticsNodes().size, "advanced before the pause")

        mainClock.advanceTimeBy(600)
        mainClock.autoAdvance = true
        assertShown("Where to?")
    }

    /**
     * Going back over steps that are already done and forward again shows
     * each of them, with a Next button, instead of bouncing through.
     */
    @Test
    fun revisitingFilledStepsDoesNotSkipThem() = runTourTest {
        fillUpToStyle()
        tap("Balanced")
        assertShown("Your route")
        tap("Back"); assertShown("Pick a pace")
        tap("Back"); assertShown("Where to?")
        tap("Back"); assertShown("Name your trip")

        tap("Next")
        assertShown("Where to?")
        // Long enough for a typing pause to have fired if it were armed on the old text.
        mainClock.advanceTimeBy(3_000)
        assertShown("Where to?")
        assertNotShown("Pick a pace")

        tap("Next")
        assertShown("Pick a pace")
        mainClock.advanceTimeBy(3_000)
        assertNotShown("Your route")
        tap("Next")
        assertShown("Your route")
    }

    /** Coming back to the route step does not load the route a second time. */
    @Test
    fun backToTheRouteStepShowsItAtOnce() = runTourTest {
        fillUpToStyle()
        tap("Balanced")
        assertShown("Your route")
        tap("Next")
        assertShown("Create it")

        mainClock.autoAdvance = false
        tap("Back")
        mainClock.advanceTimeBy(300)
        assertEquals(1, onAllNodesWithText("Your route").fetchSemanticsNodes().size, "route step not shown at once")
        assertEquals(0, onAllNodesWithText("Finding the best route").fetchSemanticsNodes().size)
        mainClock.autoAdvance = true
    }

    /** Navigating away mid-tour ends the tour, and the sequence with it. */
    @Test
    fun leavingScreenStopsTourAndSequence() = runComposeUiTest {
        lateinit var tours: SampleTours
        setContent {
            SampleTheme {
                val form = remember { NewTripFormState() }
                val tripUi = remember { TripUiState() }
                tours = rememberSampleTours(form, tripUi)
                WaypointSequenceEffect(tours.sequence)
                NewTripScreen(form = form, tour = tours.planTrip, onBack = {})
            }
        }
        runOnIdle { tours.sequence.goTo(tours.planTrip) }
        assertShown("Plan a trip, hands on")
        runOnIdle { assertTrue(tours.sequence.isActive) }

        // Staying on the hosting screen changes nothing.
        runOnIdle { tours.stopToursNotOn(Route.NewTrip) }
        assertShown("Plan a trip, hands on")

        runOnIdle { tours.stopToursNotOn(Route.Trips) }

        assertNotShown("Plan a trip, hands on")
        runOnIdle {
            assertFalse(tours.planTrip.isActive)
            assertFalse(tours.sequence.isActive)
            assertFalse(tours.planTrip.hasCompleted)
        }
    }
}
