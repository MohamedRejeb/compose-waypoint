package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * UI tests for [WaypointHint].
 *
 * Note: the beacon indicator is a Canvas inside AnimatedVisibility without a stable
 * testTag, so these tests primarily drive state via [WaypointHintState.open] calls
 * in `runOnIdle` to validate the open->tooltip flow. Click-based tests use a
 * trigger button to programmatically open the hint, which is closer to how apps
 * would exercise the API.
 */
@OptIn(ExperimentalTestApi::class)
class WaypointHintUiTest {

    private enum class HintKey { Search, Filter }

    private class RecordingPersistence : WaypointPersistence {
        val completed = mutableSetOf<String>()
        override fun isCompleted(tourId: String) = tourId in completed
        override fun markCompleted(tourId: String) { completed += tourId }
        override fun reset(tourId: String) { completed -= tourId }
        override fun resetAll() { completed.clear() }
    }

    private fun stateWith(
        hints: List<WaypointHint<HintKey>> = listOf(
            WaypointHint(
                key = HintKey.Search,
                title = "Hint title",
                description = "Hint description",
            ),
        ),
        persistence: WaypointPersistence? = null,
        groupId: String? = "hints-group",
    ) = WaypointHintState(
        hints = hints,
        persistence = persistence,
        groupId = groupId,
    )

    // -- Rendering --

    @Test
    fun `beacon content renders when hint is not dismissed`() = runComposeUiTest {
        val state = stateWith()

        setContent {
            WaypointHint(
                state = state,
                key = HintKey.Search,
                tooltipContent = { _ -> BasicText("tooltip") },
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .testTag("target-content"),
                )
            }
        }

        waitForIdle()
        onNodeWithTag("target-content").assertIsDisplayed()
        assertFalse(state.isDismissed(HintKey.Search))
    }

    @Test
    fun `beacon is suppressed when hint is dismissed at start via persistence`() = runComposeUiTest {
        val persistence = RecordingPersistence()
        persistence.completed += "hints-group:${HintKey.Search}"
        val state = stateWith(persistence = persistence)

        // Sanity check: persistence hydration should mark it dismissed.
        assertTrue(state.isDismissed(HintKey.Search))

        setContent {
            WaypointHint(
                state = state,
                key = HintKey.Search,
                tooltipContent = { scope ->
                    BasicText(
                        text = scope.title ?: "",
                        modifier = Modifier.testTag("tooltip-title"),
                    )
                },
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .testTag("target-content"),
                )
            }
        }

        waitForIdle()
        // Target content still renders, but no tooltip can be open.
        onNodeWithTag("target-content").assertIsDisplayed()

        // Attempting to open a dismissed hint is a no-op.
        runOnIdle { state.open(HintKey.Search) }
        waitForIdle()
        assertNull(state.openHintKey)
        assertTrue(onAllNodesWithTag("tooltip-title").fetchSemanticsNodes().isEmpty())
    }

    // -- Open flow --

    @Test
    fun `opening hint shows tooltip with title and description`() = runComposeUiTest {
        val state = stateWith()

        setContent {
            WaypointHint(
                state = state,
                key = HintKey.Search,
                tooltipContent = { scope ->
                    Box {
                        BasicText(
                            text = scope.title ?: "",
                            modifier = Modifier.testTag("tooltip-title"),
                        )
                        BasicText(
                            text = scope.description ?: "",
                            modifier = Modifier.testTag("tooltip-description"),
                        )
                    }
                },
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .testTag("target-content"),
                )
            }
        }

        waitForIdle()
        assertTrue(onAllNodesWithTag("tooltip-title").fetchSemanticsNodes().isEmpty())

        runOnIdle { state.open(HintKey.Search) }

        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithTag("tooltip-title").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("tooltip-title").assertTextEquals("Hint title")
        onNodeWithTag("tooltip-description").assertTextEquals("Hint description")
        assertEquals(HintKey.Search, state.openHintKey)
    }

    @Test
    fun `clicking external trigger opens the tooltip`() = runComposeUiTest {
        val state = stateWith()

        setContent {
            Box {
                WaypointHint(
                    state = state,
                    key = HintKey.Search,
                    tooltipContent = { scope ->
                        BasicText(
                            text = scope.title ?: "",
                            modifier = Modifier.testTag("tooltip-title"),
                        )
                    },
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .testTag("target-content"),
                    )
                }

                Box(
                    modifier = Modifier
                        .testTag("trigger")
                        .align(Alignment.BottomEnd)
                        .size(40.dp)
                        .clickable { state.open(HintKey.Search) },
                )
            }
        }

        waitForIdle()
        onNodeWithTag("trigger").performClick()

        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithTag("tooltip-title").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("tooltip-title").assertTextEquals("Hint title")
    }

    // -- Scope actions --

    @Test
    fun `hintScope dismiss hides tooltip and marks dismissed`() = runComposeUiTest {
        val persistence = RecordingPersistence()
        val state = stateWith(persistence = persistence)

        setContent {
            WaypointHint(
                state = state,
                key = HintKey.Search,
                tooltipContent = { scope ->
                    Box {
                        BasicText(
                            text = scope.title ?: "",
                            modifier = Modifier.testTag("tooltip-title"),
                        )
                        Box(
                            modifier = Modifier
                                .testTag("got-it")
                                .size(40.dp)
                                .clickable { scope.dismiss() },
                        )
                    }
                },
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .testTag("target-content"),
                )
            }
        }

        runOnIdle { state.open(HintKey.Search) }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithTag("got-it").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("got-it").performClick()
        waitForIdle()

        assertTrue(state.isDismissed(HintKey.Search))
        assertNull(state.openHintKey)
        assertTrue(
            "hints-group:${HintKey.Search}" in persistence.completed,
            "Persistence should record dismissal",
        )
        assertTrue(onAllNodesWithTag("tooltip-title").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `hintScope close hides tooltip without dismissing`() = runComposeUiTest {
        val state = stateWith()

        setContent {
            WaypointHint(
                state = state,
                key = HintKey.Search,
                tooltipContent = { scope ->
                    Box {
                        BasicText(
                            text = scope.title ?: "",
                            modifier = Modifier.testTag("tooltip-title"),
                        )
                        Box(
                            modifier = Modifier
                                .testTag("close")
                                .size(40.dp)
                                .clickable { scope.close() },
                        )
                    }
                },
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .testTag("target-content"),
                )
            }
        }

        runOnIdle { state.open(HintKey.Search) }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithTag("close").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("close").performClick()
        waitForIdle()

        assertFalse(state.isDismissed(HintKey.Search))
        assertNull(state.openHintKey)
        assertTrue(onAllNodesWithTag("tooltip-title").fetchSemanticsNodes().isEmpty())
    }

    // -- Reset --

    @Test
    fun `state reset un-dismisses so hint can be opened again`() = runComposeUiTest {
        val state = stateWith()

        setContent {
            WaypointHint(
                state = state,
                key = HintKey.Search,
                tooltipContent = { scope ->
                    BasicText(
                        text = scope.title ?: "",
                        modifier = Modifier.testTag("tooltip-title"),
                    )
                },
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .testTag("target-content"),
                )
            }
        }

        runOnIdle { state.dismiss(HintKey.Search) }
        waitForIdle()
        assertTrue(state.isDismissed(HintKey.Search))

        // Dismissed hint cannot be opened.
        runOnIdle { state.open(HintKey.Search) }
        waitForIdle()
        assertNull(state.openHintKey)

        runOnIdle { state.reset(HintKey.Search) }
        waitForIdle()
        assertFalse(state.isDismissed(HintKey.Search))

        // After reset, opening works again and tooltip appears.
        runOnIdle { state.open(HintKey.Search) }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithTag("tooltip-title").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("tooltip-title").assertTextEquals("Hint title")
    }

    // -- Unregistered key --

    @Test
    fun `unregistered key renders content without tooltip decoration`() = runComposeUiTest {
        // state only registers Search, but the host renders a hint for Filter.
        val state = stateWith(
            hints = listOf(WaypointHint(key = HintKey.Search, title = "Only Search")),
        )

        setContent {
            WaypointHint(
                state = state,
                key = HintKey.Filter,
                tooltipContent = { _ -> BasicText("should-not-render") },
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .testTag("target-content"),
                )
            }
        }

        waitForIdle()
        onNodeWithTag("target-content").assertIsDisplayed()
        // Unregistered key: no tooltip should be possible.
        assertNull(state.find(HintKey.Filter))
        assertTrue(onAllNodesWithText("should-not-render").fetchSemanticsNodes().isEmpty())

        // Even attempting to open Filter should not surface the tooltip because
        // WaypointHint early-returns for an unregistered key.
        runOnIdle { state.open(HintKey.Filter) }
        waitForIdle()
        assertTrue(onAllNodesWithText("should-not-render").fetchSemanticsNodes().isEmpty())
    }

    // -- Saveable round-trip --

    @Test
    @Ignore // StateRestorationTester is not available in commonTest (compose.uiTest on KMP)
    fun `rememberSaveable round-trip preserves dismissed state`() {
        // Intentionally left as a TODO: compose.uiTest in common source set does not
        // expose StateRestorationTester (it lives in androidx.compose.ui.test.junit4 on
        // Android). Revisit once an upstream multiplatform equivalent is available.
    }
}
