package com.mohamedrejeb.waypoint.material3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.core.WaypointState
import com.mohamedrejeb.waypoint.core.WaypointStep
import com.mohamedrejeb.waypoint.core.waypointTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * [WaypointMaterial3Labels]: defaults, overrides and progress formatting, on
 * the tooltip itself and passed through the hosts. Also covers the card of a
 * step without a target.
 */
@OptIn(ExperimentalTestApi::class)
class Material3LabelsUiTest {

    private val french = WaypointMaterial3Labels(
        skip = "Passer",
        next = "Suivant",
        back = "Retour",
        finish = "Terminer",
        progress = { current, total -> "$current sur $total" },
    )

    private fun middleScope() = TestStepScope(
        currentStepIndex = 1,
        totalSteps = 3,
        isFirstStep = false,
        isLastStep = false,
        title = "Title",
    )

    private fun ComposeUiTest.awaitText(text: String) {
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `default labels are English`() {
        val labels = WaypointMaterial3Labels.Default

        assertEquals("Skip", labels.skip)
        assertEquals("Next", labels.next)
        assertEquals("Back", labels.back)
        assertEquals("Finish", labels.finish)
        assertEquals("2 of 5", labels.progress(2, 5))
    }

    @Test
    fun `overriding one label keeps the other defaults`() {
        val labels = WaypointMaterial3Labels(next = "Continue")

        assertEquals("Continue", labels.next)
        assertEquals("Skip", labels.skip)
        assertEquals("1 of 2", labels.progress(1, 2))
    }

    @Test
    fun `tooltip shows default labels and progress`() = runComposeUiTest {
        setContent {
            MaterialTheme { WaypointMaterial3Tooltip(stepScope = middleScope()) }
        }

        onNodeWithText("2 of 3").assertIsDisplayed()
        onNodeWithText("Skip").assertIsDisplayed()
        onNodeWithText("Back").assertIsDisplayed()
        onNodeWithText("Next").assertIsDisplayed()
    }

    @Test
    fun `tooltip uses every custom label and the progress formatter`() = runComposeUiTest {
        setContent {
            MaterialTheme { WaypointMaterial3Tooltip(stepScope = middleScope(), labels = french) }
        }

        onNodeWithText("2 sur 3").assertIsDisplayed()
        onNodeWithText("Passer").assertIsDisplayed()
        onNodeWithText("Retour").assertIsDisplayed()
        onNodeWithText("Suivant").assertIsDisplayed()
        assertTrue(onAllNodesWithText("Next").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `finish label replaces next on the last step`() = runComposeUiTest {
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    stepScope = middleScope().copy(currentStepIndex = 2, currentStepNumber = 3, isLastStep = true),
                    labels = french,
                )
            }
        }

        onNodeWithText("Terminer").assertIsDisplayed()
        onNodeWithText("3 sur 3").assertIsDisplayed()
        assertTrue(onAllNodesWithText("Suivant").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `progress formatter receives visible step number and total`() = runComposeUiTest {
        val calls = mutableListOf<Pair<Int, Int>>()
        val labels = WaypointMaterial3Labels(
            progress = { current, total ->
                calls += current to total
                "Step $current/$total"
            },
        )
        setContent {
            MaterialTheme {
                WaypointMaterial3Tooltip(
                    // Index 4 in the step list, but second of three visible steps.
                    stepScope = middleScope().copy(currentStepIndex = 4, currentStepNumber = 2),
                    labels = labels,
                )
            }
        }

        onNodeWithText("Step 2/3").assertIsDisplayed()
        assertTrue(calls.all { it == 2 to 3 }, "formatter calls: $calls")
    }

    @Test
    fun `host passes labels to its tooltip`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a", title = "Step A"),
                WaypointStep(targetKey = "b", title = "Step B"),
            ),
        )
        setContent {
            MaterialTheme {
                WaypointMaterial3Host(state = state, labels = french) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(80.dp).waypointTarget(state, "a"))
                        Box(Modifier.size(80.dp).waypointTarget(state, "b"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        awaitText("Step A")
        onNodeWithText("1 sur 2").assertIsDisplayed()
        onNodeWithText("Passer").assertIsDisplayed()

        onNodeWithText("Suivant").performClick()
        awaitText("Step B")
        onNodeWithText("2 sur 2").assertIsDisplayed()
        onNodeWithText("Retour").assertIsDisplayed()
        onNodeWithText("Terminer").assertIsDisplayed()
    }

    @Test
    fun `overlay host passes labels to its tooltip`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "inner", title = "Inner")),
        )
        setContent {
            MaterialTheme {
                WaypointMaterial3Host(state = state) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        WaypointMaterial3OverlayHost(state = state, labels = french) {
                            Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                                Box(Modifier.size(60.dp).waypointTarget(state, "inner"))
                            }
                        }
                    }
                }
            }
        }

        runOnIdle { state.start() }
        awaitText("Inner")

        onNodeWithText("1 sur 1").assertIsDisplayed()
        onNodeWithText("Terminer").assertIsDisplayed()
        onNodeWithText("Passer").assertIsDisplayed()
    }

    @Test
    fun `step without a target renders the same card and navigates`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(title = "Welcome", description = "A short intro"),
                WaypointStep(targetKey = "a", title = "Step A"),
            ),
        )
        setContent {
            MaterialTheme {
                WaypointMaterial3Host(state = state) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(80.dp).waypointTarget(state, "a"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        awaitText("Welcome")
        onNodeWithText("A short intro").assertIsDisplayed()
        onNodeWithText("1 of 2").assertIsDisplayed()
        onNodeWithText("Skip").assertIsDisplayed()

        onNodeWithText("Next").performClick()
        awaitText("Step A")
        assertEquals(1, state.currentStepIndex)

        onNodeWithText("Back").performClick()
        awaitText("Welcome")

        onNodeWithText("Skip").performClick()
        waitForIdle()
        assertFalse(state.isActive)
    }
}
