package com.mohamedrejeb.waypoint.sample

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.mohamedrejeb.waypoint.sample.trips.MapBeaconTag
import kotlin.test.Test

/** Drives the whole app: the checklist, the sequence across screens, the theme and the beacon. */
@OptIn(ExperimentalTestApi::class)
class AppFlowTest {

    private fun runAppTest(
        autoStartTour: Boolean = false,
        block: ComposeUiTest.() -> Unit,
    ) = runComposeUiTest {
        setContent { App(autoStartTour = autoStartTour) }
        block()
    }

    private val lookAroundTitles = listOf(
        "Welcome to Trips",
        "Find a trip",
        "Your trips",
        "Start a new one",
        "Light or dark",
        "Pick up where you left off",
    )

    /** Walks chapter 1 with Next, up to and including the step titled [until]. */
    private fun ComposeUiTest.walkLookAround(until: String = lookAroundTitles.last()) {
        for (title in lookAroundTitles) {
            assertShown(title)
            if (title == until) return
            tap("Next")
        }
    }

    @Test
    fun autoStartsOnFirstLaunch() = runAppTest(autoStartTour = true) {
        assertShown("Welcome to Trips")
    }

    @Test
    fun lookAroundRunsToTheEnd() = runAppTest {
        onNodeWithContentDescription("Look around not started").assertExists()
        onNodeWithContentDescription("Play Look around").performClick()
        walkLookAround()
        tap("Done")
        assertNotShown("Pick up where you left off")
        onNodeWithContentDescription("Look around completed").assertExists()
        // A single chapter was played, so the sequence did not move on.
        assertShown("Trips")
    }

    @Test
    fun sequenceMovesToNewTrip() = runAppTest {
        tap("Continue")
        walkLookAround()
        tap("Done")
        // Completing chapter 1 starts chapter 2 on its own screen.
        assertShown("New trip")
        assertShown("Plan a trip, hands on")
    }

    @Test
    fun themeSwitchKeepsTourRunning() = runAppTest {
        onNodeWithContentDescription("Play Look around").performClick()
        walkLookAround(until = "Light or dark")
        // The step is PassThrough, so the switch works mid-tour.
        onNodeWithContentDescription("Switch to dark theme").performClick()
        onNodeWithContentDescription("Switch to light theme").assertExists()
        assertShown("Light or dark")
        tap("Next")
        assertShown("Pick up where you left off")
    }

    @Test
    fun beaconOpensTrip() = runAppTest {
        // The beacon is the clickable under the tag that is not the card itself.
        onNode(
            hasAnyAncestor(hasTestTag(MapBeaconTag)) and
                hasClickAction() and
                hasText("Lisbon to Porto").not(),
        ).performClick()
        assertShown("Stops on the map")
        assertShown("Lisbon to Porto")
    }

    @Test
    fun playingAChapterElsewhereNavigatesFirst() = runAppTest {
        onNodeWithContentDescription("Play Plan a trip").performClick()
        assertShown("New trip")
        assertShown("Plan a trip, hands on")
    }

    @Test
    fun completedChapterCanBeReplayed() = runAppTest {
        onNodeWithContentDescription("Play Look around").performClick()
        walkLookAround()
        tap("Done")
        onNodeWithContentDescription("Look around completed").assertExists()

        onNodeWithContentDescription("Play Look around").performClick()
        assertShown("Welcome to Trips")
    }
}
