package com.mohamedrejeb.waypoint.sample

import androidx.compose.runtime.remember
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.mohamedrejeb.waypoint.sample.data.SampleTrips
import com.mohamedrejeb.waypoint.sample.newtrip.NewTripFormState
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import com.mohamedrejeb.waypoint.sample.tour.SampleTours
import com.mohamedrejeb.waypoint.sample.tour.rememberSampleTours
import com.mohamedrejeb.waypoint.sample.trip.TripScreen
import com.mohamedrejeb.waypoint.sample.trip.TripUiState
import com.mohamedrejeb.waypoint.sample.trip.onSheetDismissed
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Drives chapter 3 on the Trip screen, including the steps inside the sheet. */
@OptIn(ExperimentalTestApi::class)
class KnowTripTourTest {

    private val tripUi = TripUiState()
    private lateinit var tours: SampleTours

    private fun runTourTest(block: ComposeUiTest.() -> Unit) = runComposeUiTest {
        setContent {
            SampleTheme {
                val form = remember { NewTripFormState() }
                tours = rememberSampleTours(form, tripUi)
                TripScreen(
                    trip = SampleTrips.first(),
                    ui = tripUi,
                    tour = tours.knowTrip,
                    onBack = {},
                )
            }
        }
        block()
    }

    private fun ComposeUiTest.reachSheet() {
        tap("Tour")
        assertShown("Stops on the map")
        tap("Next")
        assertShown("The whole route")
        tap("Next")
        assertShown("Add a stop")
        // ClickToAdvance: the tap on the highlighted button moves the tour on.
        tap("Add stop")
        assertShown("Name the stop")
    }

    @Test
    fun crossesIntoSheet() = runTourTest {
        reachSheet()
        field("Stop name").assertExists()
        tap("Next")
        assertShown("How long?")
        tap("Next")
        assertShown("Ready to share")
        // The last step closed the sheet again.
        assertNotShown("New stop")
        tap("Done")
        assertNotShown("Ready to share")
        runOnIdle {
            assertFalse(tours.knowTrip.isActive)
            assertTrue(tours.knowTrip.hasCompleted)
        }
    }

    @Test
    fun dismissingSheetStopsTour() = runTourTest {
        reachSheet()
        runOnIdle { onSheetDismissed(tripUi, tours.knowTrip) }
        assertNotShown("Name the stop")
        runOnIdle {
            assertFalse(tours.knowTrip.isActive)
            assertFalse(tripUi.sheetOpen)
        }
    }
}
