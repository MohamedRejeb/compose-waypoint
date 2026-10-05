package com.mohamedrejeb.waypoint.sample.demos

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.mohamedrejeb.waypoint.sample.demos.modals.ModalToursDemo
import com.mohamedrejeb.waypoint.sample.demos.onboarding.OnboardingDemo
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** End-to-end smoke tests of the Onboarding and Dialogs & sheets demos. */
@OptIn(ExperimentalTestApi::class)
class GuidedTourDemosTest {

    private fun runDemoTest(demo: @Composable () -> Unit, block: ComposeUiTest.() -> Unit) = runComposeUiTest {
        setContent {
            SampleTheme { demo() }
        }
        block()
    }

    private fun ComposeUiTest.assertShown(text: String) {
        waitUntil(timeoutMillis = 5_000) { onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun ComposeUiTest.assertNotShown(text: String) {
        waitForIdle()
        assertEquals(0, onAllNodesWithText(text).fetchSemanticsNodes().size, "\"$text\" should not be shown")
    }

    @Test
    fun onboarding_runsToTheEnd_andLogsAnalytics() = runDemoTest({ OnboardingDemo(onBack = {}) }) {
        onNodeWithText("Start tour").performClick()
        assertShown("Find tasks, projects, and teammates from one place.")
        onNodeWithText("Next").performClick()
        assertShown("Mentions and updates land here.")
        onNodeWithText("Next").performClick()
        assertShown("Your week at a glance")
        onNodeWithText("Next").performClick()
        assertShown("Try it")

        // ClickToAdvance on the last step: tapping the highlighted task ends the tour.
        onNodeWithText("Review pull request").performClick()
        assertShown("tour_completed")
        assertNotShown("Try it")
        assertShown("Start tour")
    }

    @Test
    fun dialogTour_opensTheDialog_andClosesItWhenDone() = runDemoTest({ ModalToursDemo(onBack = {}) }) {
        // The dialog section's button is the first of the three "Start tour" buttons.
        onAllNodesWithText("Start tour")[0].performClick()
        assertShown("The tour starts on the button that opens the dialog.")
        assertNotShown("Settings")

        onNodeWithText("Next").performClick()
        assertShown("beforeShow opened the dialog so this row could be highlighted.")
        onNodeWithText("Settings").assertExists()

        onNodeWithText("Next").performClick()
        assertShown("The overlay keeps tracking targets across dialog steps.")

        onNodeWithText("Finish").performClick()
        assertNotShown("Settings")
        assertNotShown("The overlay keeps tracking targets across dialog steps.")
    }
}
