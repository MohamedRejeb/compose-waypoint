package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CompletableDeferred
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isNotFocusable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Keyboard handling during a [TargetInteraction.PassThrough] step: the user
 * may be typing in the target, so next and previous keys belong to the app
 * and only the dismiss keys are handled by the host.
 */
@OptIn(ExperimentalTestApi::class)
class PassThroughKeyboardUiTest {

    private fun runKeyboardTest(
        block: ComposeUiTest.(state: WaypointState<String>, appKeys: List<Key>) -> Unit,
    ) = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "intro", title = "Intro"),
                WaypointStep(
                    targetKey = "field",
                    title = "Field",
                    interaction = TargetInteraction.PassThrough,
                ),
                WaypointStep(targetKey = "intro", title = "Outro"),
            ),
        )
        var text by mutableStateOf("")
        // Key presses that made it to the app's own handler on the field.
        val appKeys = mutableListOf<Key>()

        setContent {
            WaypointHost(
                state = state,
                modifier = Modifier.testTag("host"),
                tooltipContent = { scope -> BasicText("tip-${scope.title}") },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column {
                        Box(Modifier.size(60.dp).waypointTarget(state, "intro"))
                        BasicTextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier
                                .size(200.dp, 48.dp)
                                .onPreviewKeyEvent { event ->
                                    if (event.type == KeyEventType.KeyDown) appKeys += event.key
                                    false
                                }
                                .waypointTarget(state, "field")
                                .testTag("field"),
                        )
                    }
                }
            }
        }

        runOnIdle { state.start() }
        awaitTip("Intro")
        runOnIdle { state.next() }
        awaitTip("Field")

        // The user starts working in the target.
        onNodeWithTag("field").performClick()
        waitForIdle()
        onNodeWithTag("field").assertIsFocused()

        block(state, appKeys)
    }

    private fun ComposeUiTest.awaitTip(title: String) {
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("tip-$title").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()
    }

    @Test
    fun `next keys go to the app during a PassThrough step`() = runKeyboardTest { state, appKeys ->
        onNodeWithTag("field").performKeyInput {
            pressKey(Key.DirectionRight)
            pressKey(Key.Enter)
        }
        waitForIdle()

        assertEquals(1, state.currentStepIndex, "the tour must not advance")
        assertTrue(Key.DirectionRight in appKeys, "app did not receive the right arrow: $appKeys")
        assertTrue(Key.Enter in appKeys, "app did not receive Enter: $appKeys")
        onNodeWithTag("field").assertIsFocused()
    }

    @Test
    fun `previous keys go to the app during a PassThrough step`() = runKeyboardTest { state, appKeys ->
        onNodeWithTag("field").performKeyInput { pressKey(Key.DirectionLeft) }
        waitForIdle()

        assertEquals(1, state.currentStepIndex, "the tour must not go back")
        assertTrue(Key.DirectionLeft in appKeys, "app did not receive the left arrow: $appKeys")
    }

    @Test
    fun `dismiss key still cancels the tour during a PassThrough step`() = runKeyboardTest { state, appKeys ->
        onNodeWithTag("field").performKeyInput { pressKey(Key.Escape) }
        waitForIdle()

        assertFalse(state.isActive)
        assertEquals(WaypointEndReason.Cancelled, state.lastEndReason)
        assertFalse(Key.Escape in appKeys, "the dismiss key is handled by the host")
    }

    @Test
    fun `navigation keys work again on the step after a PassThrough step`() = runKeyboardTest { state, _ ->
        runOnIdle { state.next() }
        awaitTip("Outro")

        onNodeWithTag("host").performKeyInput { pressKey(Key.DirectionLeft) }
        waitForIdle()

        assertEquals(1, state.currentStepIndex)
    }

    @Test
    fun `keys typed in the app are not stolen while a gate holds the step`() = runComposeUiTest {
        val gate = CompletableDeferred<Unit>()
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "field", title = "Field", interaction = TargetInteraction.PassThrough),
                WaypointStep(targetKey = "field", title = "Held", beforeShow = { gate.await() }),
                WaypointStep(targetKey = "field", title = "Last"),
            ),
        )
        val appKeys = mutableListOf<Key>()
        setContent {
            WaypointHost(
                state = state,
                modifier = Modifier.testTag("host"),
                tooltipContent = { scope -> BasicText("tip-${scope.title}") },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    BasicTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier
                            .size(200.dp, 48.dp)
                            .onPreviewKeyEvent { event ->
                                if (event.type == KeyEventType.KeyDown) appKeys += event.key
                                false
                            }
                            .waypointTarget(state, "field")
                            .testTag("field"),
                    )
                }
            }
        }
        runOnIdle { state.start() }
        awaitTip("Field")
        onNodeWithTag("field").performClick()
        waitForIdle()
        onNodeWithTag("field").assertIsFocused()

        runOnIdle { state.next() }
        waitForIdle()
        assertFalse(state.isStepVisible)

        onNodeWithTag("field").performKeyInput {
            pressKey(Key.Enter)
            pressKey(Key.DirectionRight)
        }
        waitForIdle()

        assertEquals(1, state.currentStepIndex, "navigation keys moved a held step")
        assertTrue(Key.Enter in appKeys && Key.DirectionRight in appKeys, "app did not get the keys: $appKeys")

        // Escape still ends the tour.
        onNodeWithTag("field").performKeyInput { pressKey(Key.Escape) }
        waitForIdle()
        assertFalse(state.isActive)
    }

    @Test
    fun `host is not focusable while the tour is inactive`() = runComposeUiTest {
        val state = WaypointState(steps = listOf(WaypointStep(targetKey = "a", title = "A")))
        setContent {
            WaypointHost(
                state = state,
                modifier = Modifier.testTag("host"),
                tooltipContent = { scope -> BasicText("tip-${scope.title}") },
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "a"))
                }
            }
        }
        waitForIdle()
        onNodeWithTag("host").assert(isNotFocusable())

        runOnIdle { state.start() }
        awaitTip("A")
        onNodeWithTag("host").assert(isFocusable())
        onNodeWithTag("host").assertIsFocused()

        runOnIdle { state.stop() }
        waitForIdle()
        onNodeWithTag("host").assert(isNotFocusable())
    }
}
