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
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that targets with [TargetInteraction.AllowClick] allow the user
 * to interact with the underlying composable (e.g., type in a text field)
 * during a tour step.
 *
 * Known bugs:
 * 1. The spotlight overlay intercepts ALL touch events via `pointerInput { detectTapGestures }`,
 *    preventing taps from reaching the target even when AllowClick is set.
 * 2. WaypointHost's keyboard FocusRequester steals focus from text fields
 *    when the tour is active.
 */
@OptIn(ExperimentalTestApi::class)
class TargetInteractionUiTest {

    // Fixed: overlay passes taps through in target area + onPreviewKeyEvent doesn't steal focus
    @Test
    fun `text field target is typeable when AllowClick is set`() = runComposeUiTest {
        val textState = mutableStateOf("")

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "search",
                    title = "Try searching",
                    interaction = TargetInteraction.AllowClick,
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicTextField(
                        value = textState.value,
                        onValueChange = { textState.value = it },
                        modifier = Modifier
                            .size(200.dp, 48.dp)
                            .waypointTarget(state, "search")
                            .testTag("search-field"),
                    )
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Click the text field to focus it
        onNodeWithTag("search-field").performClick()
        waitForIdle()

        // The text field should be focused and accept input
        onNodeWithTag("search-field").assertIsFocused()

        // Type text
        onNodeWithTag("search-field").performTextInput("hello")
        waitForIdle()

        assertEquals("hello", textState.value)
    }

    // Fixed: overlay passes taps through in target area + onPreviewKeyEvent doesn't steal focus
    @Test
    fun `button target is clickable when AllowClick is set`() = runComposeUiTest {
        var clicked = false

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "btn",
                    interaction = TargetInteraction.AllowClick,
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
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
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Click the button target
        onNodeWithTag("button").performClick()
        waitForIdle()

        // The click should have reached the actual button
        assertTrue(clicked, "Button click did not propagate through overlay")
    }

    private fun assertTrue(value: Boolean, message: String) {
        kotlin.test.assertTrue(value, message)
    }

    // -- Focus Clearing on Step Transition --

    // Fixed: focusManager.clearFocus() on step transition
    @Test
    fun `text field loses focus when tour advances to next step`() = runComposeUiTest {
        val textState = mutableStateOf("")

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "field",
                    interaction = TargetInteraction.AllowClick,
                ),
                WaypointStep(targetKey = "other"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.targetKey ?: "",
                        modifier = Modifier.testTag("tooltip"),
                    )
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
                                .size(60.dp)
                                .waypointTarget(state, "other")
                                .testTag("other-target"),
                        )
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("field").fetchSemanticsNodes().isNotEmpty()
        }

        // Focus the text field and type
        onNodeWithTag("text-field").performClick()
        waitForIdle()
        onNodeWithTag("text-field").assertIsFocused()
        onNodeWithTag("text-field").performTextInput("hi")
        waitForIdle()

        // Advance to next step
        runOnIdle { state.next() }
        waitForIdle()

        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("other").fetchSemanticsNodes().isNotEmpty()
        }

        // The text field should no longer be focused
        onNodeWithTag("text-field").assertIsNotFocused()
    }

    // Fixed: focusManager.clearFocus() on step transition
    @Test
    fun `text field loses focus when tour is stopped`() = runComposeUiTest {
        val textState = mutableStateOf("")

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "field",
                    interaction = TargetInteraction.AllowClick,
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicTextField(
                        value = textState.value,
                        onValueChange = { textState.value = it },
                        modifier = Modifier
                            .size(200.dp, 48.dp)
                            .waypointTarget(state, "field")
                            .testTag("text-field"),
                    )
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Focus and type
        onNodeWithTag("text-field").performClick()
        waitForIdle()
        onNodeWithTag("text-field").assertIsFocused()

        // Stop the tour
        runOnIdle { state.stop() }
        waitForIdle()

        // Focus should be cleared
        onNodeWithTag("text-field").assertIsNotFocused()
    }
}
