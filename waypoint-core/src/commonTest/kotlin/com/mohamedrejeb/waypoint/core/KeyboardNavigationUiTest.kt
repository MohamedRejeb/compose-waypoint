package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class KeyboardNavigationUiTest {

    private fun threeStepState() = WaypointState(
        steps = listOf(
            WaypointStep(targetKey = "a", title = "Step A"),
            WaypointStep(targetKey = "b", title = "Step B"),
            WaypointStep(targetKey = "c", title = "Step C"),
        ),
    )

    /**
     * Sets up WaypointHost with three steps and a tooltip showing the title.
     * Starts the tour and waits for the first tooltip to appear.
     * Returns the state for further assertions.
     */
    private fun runKeyboardTest(
        keyboardConfig: KeyboardConfig = KeyboardConfig.Default,
        onTourComplete: (() -> Unit)? = null,
        onTourCancel: (() -> Unit)? = null,
        block: androidx.compose.ui.test.ComposeUiTest.(WaypointState<String>) -> Unit,
    ) = runComposeUiTest {
        val state = threeStepState()

        setContent {
            WaypointHost(
                state = state,
                keyboardConfig = keyboardConfig,
                onTourComplete = onTourComplete,
                onTourCancel = onTourCancel,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.title ?: "",
                        modifier = Modifier.testTag("tooltip"),
                    )
                },
                modifier = Modifier.testTag("host"),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column {
                        Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                        Box(Modifier.size(60.dp).waypointTarget(state, "b"))
                        Box(Modifier.size(60.dp).waypointTarget(state, "c"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step A").fetchSemanticsNodes().isNotEmpty()
        }

        block(state)
    }

    // -- Right Arrow --

    @Test
    fun `right arrow advances to next step`() = runKeyboardTest { state ->
        onNodeWithTag("host").performKeyInput { pressKey(Key.DirectionRight) }
        waitForIdle()

        assertEquals(1, state.currentStepIndex)
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step B").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("tooltip").assertTextEquals("Step B")
    }

    // -- Enter --

    @Test
    fun `enter advances to next step`() = runKeyboardTest { state ->
        onNodeWithTag("host").performKeyInput { pressKey(Key.Enter) }
        waitForIdle()

        assertEquals(1, state.currentStepIndex)
    }

    // -- Left Arrow --

    @Test
    fun `left arrow goes to previous step`() = runKeyboardTest { state ->
        // First advance to step B
        onNodeWithTag("host").performKeyInput { pressKey(Key.DirectionRight) }
        waitForIdle()
        assertEquals(1, state.currentStepIndex)

        // Now go back
        onNodeWithTag("host").performKeyInput { pressKey(Key.DirectionLeft) }
        waitForIdle()

        assertEquals(0, state.currentStepIndex)
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step A").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun `left arrow on first step is no-op`() = runKeyboardTest { state ->
        onNodeWithTag("host").performKeyInput { pressKey(Key.DirectionLeft) }
        waitForIdle()

        assertEquals(0, state.currentStepIndex) // didn't move
    }

    // -- Escape --

    @Test
    fun `escape dismisses the tour`() = runKeyboardTest { state ->
        onNodeWithTag("host").performKeyInput { pressKey(Key.Escape) }
        waitForIdle()

        assertFalse(state.isActive)
    }

    @Test
    fun `escape fires onTourCancel`() {
        var cancelled = false
        runKeyboardTest(onTourCancel = { cancelled = true }) { _ ->
            onNodeWithTag("host").performKeyInput { pressKey(Key.Escape) }
            waitForIdle()

            assertTrue(cancelled)
        }
    }

    // -- Tour completion via keyboard --

    @Test
    fun `right arrow on last step completes tour`() {
        var completed = false
        runKeyboardTest(onTourComplete = { completed = true }) { state ->
            // Advance to last step
            onNodeWithTag("host").performKeyInput { pressKey(Key.DirectionRight) } // → B
            waitForIdle()
            onNodeWithTag("host").performKeyInput { pressKey(Key.DirectionRight) } // → C
            waitForIdle()
            assertEquals(2, state.currentStepIndex)

            // One more → should complete
            onNodeWithTag("host").performKeyInput { pressKey(Key.DirectionRight) }
            waitForIdle()

            assertFalse(state.isActive)
            assertTrue(completed)
        }
    }

    // -- Disabled keyboard --

    @Test
    fun `keys are no-op when keyboard disabled`() = runKeyboardTest(
        keyboardConfig = KeyboardConfig.Disabled,
    ) { state ->
        onNodeWithTag("host").performKeyInput { pressKey(Key.DirectionRight) }
        waitForIdle()

        // Should still be on step 0
        assertEquals(0, state.currentStepIndex)
        assertTrue(state.isActive)
    }

    @Test
    fun `escape is no-op when keyboard disabled`() = runKeyboardTest(
        keyboardConfig = KeyboardConfig.Disabled,
    ) { state ->
        onNodeWithTag("host").performKeyInput { pressKey(Key.Escape) }
        waitForIdle()

        assertTrue(state.isActive) // not dismissed
    }

    // -- Custom key bindings --

    @Test
    fun `custom next key works`() = runKeyboardTest(
        keyboardConfig = KeyboardConfig(
            nextKeys = setOf(Key.Spacebar),
            previousKeys = setOf(Key.Backspace),
            dismissKeys = setOf(Key.Q),
        ),
    ) { state ->
        onNodeWithTag("host").performKeyInput { pressKey(Key.Spacebar) }
        waitForIdle()

        assertEquals(1, state.currentStepIndex)
    }

    @Test
    fun `custom dismiss key works`() = runKeyboardTest(
        keyboardConfig = KeyboardConfig(
            nextKeys = setOf(Key.Spacebar),
            previousKeys = setOf(Key.Backspace),
            dismissKeys = setOf(Key.Q),
        ),
    ) { state ->
        onNodeWithTag("host").performKeyInput { pressKey(Key.Q) }
        waitForIdle()

        assertFalse(state.isActive)
    }

    @Test
    fun `default keys are ignored when custom bindings set`() = runKeyboardTest(
        keyboardConfig = KeyboardConfig(
            nextKeys = setOf(Key.Spacebar),
            previousKeys = setOf(Key.Backspace),
            dismissKeys = setOf(Key.Q),
        ),
    ) { state ->
        // Default Right arrow should not work
        onNodeWithTag("host").performKeyInput { pressKey(Key.DirectionRight) }
        waitForIdle()

        assertEquals(0, state.currentStepIndex) // didn't advance

        // Default Escape should not work
        onNodeWithTag("host").performKeyInput { pressKey(Key.Escape) }
        waitForIdle()

        assertTrue(state.isActive) // not dismissed
    }
}
