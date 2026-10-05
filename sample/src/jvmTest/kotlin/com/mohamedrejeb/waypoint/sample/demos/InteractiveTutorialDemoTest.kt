package com.mohamedrejeb.waypoint.sample.demos

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.mohamedrejeb.waypoint.sample.demos.tutorial.InteractiveTutorialDemo
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Drives the Interactive Tutorial demo the way a user would: real clicks
 * through the root (so the spotlight's touch blocking is exercised) and typing
 * into the highlighted fields.
 */
@OptIn(ExperimentalTestApi::class)
class InteractiveTutorialDemoTest {

    private fun runTutorialTest(block: ComposeUiTest.() -> Unit) = runComposeUiTest {
        setContent {
            SampleTheme {
                InteractiveTutorialDemo(onBack = {})
            }
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

    private fun ComposeUiTest.field(label: String): SemanticsNodeInteraction =
        onNode(hasSetTextAction() and hasText(label))

    /** Welcome card, then name and email typed inside the highlighted fields. */
    private fun ComposeUiTest.fillNameAndEmail() {
        onNodeWithText("Start tour").performClick()
        assertShown("Create an account, hands on")
        onNodeWithText("Next").performClick()

        assertShown("Enter your name")
        // Everything outside the highlighted field is blocked.
        onNodeWithText("Create account").performClick()
        assertNotShown("Account created")
        // The click lands in the field itself and focuses it.
        field("Name").performClick()
        field("Name").assertIsFocused()
        field("Name").performTextInput("Al")

        assertShown("Add your email")
        // The step change did not take focus away, the user can keep typing.
        field("Name").assertIsFocused()
        field("Name").performTextInput("ice")
        field("Alice").assertExists()

        field("Email").performClick()
        field("Email").assertIsFocused()
        field("Email").performTextInput("alice@example.com")
        assertShown("Pick a plan")
    }

    /** Picks [plan] and waits out the summary gate, checking the form is disabled meanwhile. */
    private fun ComposeUiTest.pickPlanAndWaitForSummary(plan: String) {
        mainClock.autoAdvance = false
        onNodeWithText(plan).performClick()
        mainClock.advanceTimeBy(600)
        onNodeWithText("Preparing your summary").assertExists()
        assertEquals(0, onAllNodesWithText("Your summary").fetchSemanticsNodes().size)
        // A disabled text field drops its SetText action, so match on the text alone.
        onNodeWithText("Alice").assertIsNotEnabled()
        mainClock.autoAdvance = true
        assertShown("Your summary")
    }

    @Test
    fun paidPlan_runsEveryStep() = runTutorialTest {
        fillNameAndEmail()
        pickPlanAndWaitForSummary("Pro")
        onNodeWithText("Next").performClick()

        assertShown("Create your account")
        // The highlighted button receives the real click.
        onNodeWithText("Create account").performClick()
        assertShown("Account created")

        assertShown("That's the whole form")
        onNodeWithText("Finish").performClick()
        assertShown("Start tour")
        assertNotShown("That's the whole form")
    }

    @Test
    fun freePlan_skipsTheCreateStep() = runTutorialTest {
        fillNameAndEmail()
        pickPlanAndWaitForSummary("Free")
        onNodeWithText("Next").performClick()

        assertShown("That's the whole form")
        assertNotShown("Create your account")
        onNodeWithText("Finish").performClick()
        assertShown("Start tour")
    }
}
