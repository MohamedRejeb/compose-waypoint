package com.mohamedrejeb.waypoint.sample

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.mohamedrejeb.waypoint.sample.lab.LabScreen
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import kotlin.test.Test

/** The Lab's controls act on a running tour. */
@OptIn(ExperimentalTestApi::class)
class LabTest {

    private fun runLabTest(block: ComposeUiTest.() -> Unit) = runComposeUiTest {
        setContent { SampleTheme { LabScreen(onBack = {}) } }
        block()
    }

    @Test
    fun tooltipSwitchesToMaterial3WhileRunning() = runLabTest {
        tap("Start tour")
        assertShown("The highlight")
        assertNotShown("1 of 3")
        tap("Material3")
        // Same step, now drawn by the Material3 tooltip with its text progress.
        assertShown("The highlight")
        assertShown("1 of 3")
    }

    @Test
    fun configChangeKeepsTheCurrentStep() = runLabTest {
        tap("Start tour")
        tap("2")
        assertShown("The placement")
        tap("Pulse")
        assertShown("The placement")
    }

    @Test
    fun pauseAndResume() = runLabTest {
        tap("Start tour")
        assertShown("The highlight")
        tap("Pause")
        assertNotShown("The highlight")
        tap("Resume")
        assertShown("The highlight")
    }

    @Test
    fun eventsAndEndReasonAreLogged() = runLabTest {
        assertShown("ended: not yet")
        tap("Start tour")
        assertShown("tour_started lab steps=3")
        tap("Stop tour")
        assertShown("tour_cancelled lab at=0")
        assertShown("ended: Cancelled")
    }
}
