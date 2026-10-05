package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests that a target with [TargetInteraction.PassThrough] can be used
 * normally during its step (clicked, focused, typed into), and that the host
 * leaves keyboard focus alone while the user works inside such a target.
 */
@OptIn(ExperimentalTestApi::class)
class TargetInteractionUiTest {

    private fun ComposeUiTest.awaitText(text: String) {
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** A host with a text field target ("field") above a plain target ("other"). */
    private fun ComposeUiTest.setFieldContent(
        state: WaypointState<String>,
        textState: androidx.compose.runtime.MutableState<String>,
        keyboardConfig: KeyboardConfig = KeyboardConfig.Default,
    ) {
        setContent {
            WaypointHost(
                state = state,
                keyboardConfig = keyboardConfig,
                tooltipContent = { _ ->
                    BasicText("tip-${state.currentStep?.targetKey}")
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column {
                        BasicTextField(
                            value = textState.value,
                            onValueChange = { textState.value = it },
                            modifier = Modifier
                                .size(200.dp, 48.dp)
                                .waypointTarget(state, "field")
                                .testTag("text-field"),
                        )
                        Box(
                            modifier = Modifier
                                .size(200.dp, 48.dp)
                                .waypointTarget(state, "other"),
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `text field target is typeable when PassThrough is set`() = runComposeUiTest {
        val textState = mutableStateOf("")
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "field", interaction = TargetInteraction.PassThrough),
            ),
        )
        setFieldContent(state, textState)

        runOnIdle { state.start() }
        awaitText("tip-field")

        // Click the text field to focus it
        onNodeWithTag("text-field").performClick()
        waitForIdle()
        onNodeWithTag("text-field").assertIsFocused()

        onNodeWithTag("text-field").performTextInput("hello")
        waitForIdle()

        assertEquals("hello", textState.value)
    }

    @Test
    fun `button target is clickable when PassThrough is set`() = runComposeUiTest {
        var clicked = false

        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "btn", interaction = TargetInteraction.PassThrough),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _ -> BasicText("Tooltip") },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { clicked = true }
                            .waypointTarget(state, "btn")
                            .testTag("button"),
                    ) {
                        BasicText("Click Me")
                    }
                }
            }
        }

        runOnIdle { state.start() }
        awaitText("Tooltip")

        onNodeWithTag("button").performClick()
        waitForIdle()

        assertTrue(clicked, "Button click did not propagate through overlay")
    }

    // Focus is left to the app during pass-through steps

    @Test
    fun `text field keeps focus when the tour moves between PassThrough steps`() = runComposeUiTest {
        val textState = mutableStateOf("")
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "field", interaction = TargetInteraction.PassThrough),
                WaypointStep(
                    targetKey = "other",
                    interaction = TargetInteraction.PassThrough,
                    additionalTargets = listOf("field"),
                ),
            ),
        )
        setFieldContent(state, textState)

        runOnIdle { state.start() }
        awaitText("tip-field")

        onNodeWithTag("text-field").performClick()
        waitForIdle()
        onNodeWithTag("text-field").assertIsFocused()
        onNodeWithTag("text-field").performTextInput("hi")

        runOnIdle { state.next() }
        awaitText("tip-other")

        // The user is still working in the field: it stays focused and typeable.
        onNodeWithTag("text-field").assertIsFocused()
        onNodeWithTag("text-field").performTextInput("!")
        waitForIdle()
        assertEquals("hi!", textState.value)
    }

    @Test
    fun `text field keeps focus when the tour is stopped`() = runComposeUiTest {
        val textState = mutableStateOf("")
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "field", interaction = TargetInteraction.PassThrough),
            ),
        )
        setFieldContent(state, textState)

        runOnIdle { state.start() }
        awaitText("tip-field")

        onNodeWithTag("text-field").performClick()
        waitForIdle()
        onNodeWithTag("text-field").assertIsFocused()

        runOnIdle { state.stop() }
        waitForIdle()

        // Ending the tour does not touch the app's focus.
        onNodeWithTag("text-field").assertIsFocused()
    }

    @Test
    fun `host takes focus for keyboard navigation on a step that is not PassThrough`() = runComposeUiTest {
        val textState = mutableStateOf("")
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "field", interaction = TargetInteraction.PassThrough),
                WaypointStep(targetKey = "other"),
            ),
        )
        setFieldContent(state, textState)

        runOnIdle { state.start() }
        awaitText("tip-field")

        onNodeWithTag("text-field").performClick()
        waitForIdle()
        onNodeWithTag("text-field").assertIsFocused()

        runOnIdle { state.next() }
        awaitText("tip-other")

        // The next step is navigated with the keyboard, so the host owns focus.
        onNodeWithTag("text-field").assertIsNotFocused()
    }

    @Test
    fun `host never takes focus when keyboard navigation is disabled`() = runComposeUiTest {
        val textState = mutableStateOf("")
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "field", interaction = TargetInteraction.PassThrough),
                WaypointStep(targetKey = "other"),
            ),
        )
        setFieldContent(state, textState, keyboardConfig = KeyboardConfig.Disabled)

        runOnIdle { state.start() }
        awaitText("tip-field")

        onNodeWithTag("text-field").performClick()
        waitForIdle()
        onNodeWithTag("text-field").assertIsFocused()

        runOnIdle { state.next() }
        awaitText("tip-other")

        onNodeWithTag("text-field").assertIsFocused()
    }
}
