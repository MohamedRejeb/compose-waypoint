package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class WaypointHostUiTest {

    private fun twoStepState() = WaypointState(
        steps = listOf(
            WaypointStep(targetKey = "first", title = "Step One", description = "First description"),
            WaypointStep(targetKey = "second", title = "Step Two", description = "Second description"),
        ),
    )

    // -- Overlay Visibility --

    @Test
    fun `overlay not shown when tour inactive`() = runComposeUiTest {
        val state = twoStepState()

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.size(100.dp).waypointTarget(state, "first"))
            }
        }

        waitForIdle()
        onNodeWithTag("tooltip").assertDoesNotExist()
    }

    @Test
    fun `tooltip shown when tour starts`() = runComposeUiTest {
        val state = twoStepState()

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.title ?: "",
                        modifier = Modifier.testTag("tooltip-title"),
                    )
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                }
            }
        }

        runOnIdle { state.start() }

        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step One").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("tooltip-title").assertTextEquals("Step One")
    }

    // -- Tooltip Content --

    @Test
    fun `tooltip displays step title and description`() = runComposeUiTest {
        val state = twoStepState()

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    Column {
                        BasicText(
                            text = state.currentStep?.title ?: "",
                            modifier = Modifier.testTag("title"),
                        )
                        BasicText(
                            text = state.currentStep?.description ?: "",
                            modifier = Modifier.testTag("desc"),
                        )
                    }
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                }
            }
        }

        runOnIdle { state.start() }

        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step One").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("title").assertTextEquals("Step One")
        onNodeWithTag("desc").assertTextEquals("First description")
    }

    // -- Navigation --

    @Test
    fun `next advances to next step`() = runComposeUiTest {
        val state = twoStepState()

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.title ?: "",
                        modifier = Modifier.testTag("title"),
                    )
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column {
                        Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                        Box(Modifier.size(100.dp).waypointTarget(state, "second"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step One").fetchSemanticsNodes().isNotEmpty()
        }

        runOnIdle { state.next() }
        waitForIdle()

        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step Two").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("title").assertTextEquals("Step Two")
    }

    @Test
    fun `previous goes back to prior step`() = runComposeUiTest {
        val state = twoStepState()

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.title ?: "",
                        modifier = Modifier.testTag("title"),
                    )
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column {
                        Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                        Box(Modifier.size(100.dp).waypointTarget(state, "second"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step One").fetchSemanticsNodes().isNotEmpty()
        }

        runOnIdle { state.next() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step Two").fetchSemanticsNodes().isNotEmpty()
        }

        runOnIdle { state.previous() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Step One").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("title").assertTextEquals("Step One")
    }

    @Test
    fun `stop hides tooltip`() = runComposeUiTest {
        val state = twoStepState()

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip visible", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "first"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip visible").fetchSemanticsNodes().isNotEmpty()
        }

        runOnIdle { state.stop() }
        waitForIdle()

        onNodeWithTag("tooltip").assertDoesNotExist()
    }

    @Test
    fun `next on last step completes tour`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "only")),
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
                    Box(Modifier.size(100.dp).waypointTarget(state, "only"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        runOnIdle { state.next() }
        waitForIdle()

        assertFalse(state.isActive)
        onNodeWithTag("tooltip").assertDoesNotExist()
    }

    // -- Custom Tooltip Content --

    @Test
    fun `custom step content renders instead of host tooltip`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "custom",
                    content = { _ ->
                        BasicText("Custom Content", Modifier.testTag("custom-tooltip"))
                    },
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Default Content", Modifier.testTag("default-tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "custom"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Custom Content").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("custom-tooltip").assertIsDisplayed()
        onNodeWithTag("default-tooltip").assertDoesNotExist()
    }

    // -- StepScope Properties --

    @Test
    fun `step scope provides correct step info`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "a"),
                WaypointStep(targetKey = "b"),
                WaypointStep(targetKey = "c"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { scope, _ ->
                    Column {
                        BasicText("index:${scope.currentStepIndex}", Modifier.testTag("index"))
                        BasicText("total:${scope.totalSteps}", Modifier.testTag("total"))
                        BasicText("first:${scope.isFirstStep}", Modifier.testTag("first"))
                        BasicText("last:${scope.isLastStep}", Modifier.testTag("last"))
                    }
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column {
                        Box(Modifier.size(50.dp).waypointTarget(state, "a"))
                        Box(Modifier.size(50.dp).waypointTarget(state, "b"))
                        Box(Modifier.size(50.dp).waypointTarget(state, "c"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("index:0").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("index").assertTextEquals("index:0")
        onNodeWithTag("total").assertTextEquals("total:3")
        onNodeWithTag("first").assertTextEquals("first:true")
        onNodeWithTag("last").assertTextEquals("last:false")

        // Move to last step
        runOnIdle { state.next() }
        waitForIdle()
        runOnIdle { state.next() }
        waitForIdle()

        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("index:2").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("index").assertTextEquals("index:2")
        onNodeWithTag("first").assertTextEquals("first:false")
        onNodeWithTag("last").assertTextEquals("last:true")
    }

    // -- Pause/Resume in UI --

    @Test
    fun `pause hides tooltip, resume shows it again`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(WaypointStep(targetKey = "target")),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Visible", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "target"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Visible").fetchSemanticsNodes().isNotEmpty()
        }

        runOnIdle { state.pause() }
        waitForIdle()
        onNodeWithTag("tooltip").assertDoesNotExist()

        runOnIdle { state.resume() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Visible").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("tooltip").assertIsDisplayed()
    }

    // -- Highlight Style Variants --

    /**
     * Helper: create a host with a given highlight style, start the tour,
     * and verify the tooltip appears (tooltip should show regardless of highlight style).
     */
    private fun runHighlightStyleTest(
        highlightStyle: HighlightStyle,
        block: androidx.compose.ui.test.ComposeUiTest.() -> Unit = {},
    ) = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = highlightStyle,
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip Text", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "target"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip Text").fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithTag("tooltip").assertIsDisplayed()

        block()
    }

    @Test
    fun `HighlightStyle Default resolves to Spotlight`() {
        assertTrue(HighlightStyle.Default is HighlightStyle.Spotlight)
    }

    @Test
    fun `tooltip shows with Spotlight highlight`() = runHighlightStyleTest(
        highlightStyle = HighlightStyle.Spotlight(),
    )

    @Test
    fun `tooltip shows with Pulse highlight`() = runHighlightStyleTest(
        highlightStyle = HighlightStyle.Pulse(color = Color.Blue),
    )

    @Test
    fun `tooltip shows with Border highlight`() = runHighlightStyleTest(
        highlightStyle = HighlightStyle.Border(color = Color.Red),
    )

    @Test
    fun `tooltip shows with Ripple highlight`() = runHighlightStyleTest(
        highlightStyle = HighlightStyle.Ripple(color = Color.Green),
    )

    @Test
    fun `tooltip shows with None highlight`() = runHighlightStyleTest(
        highlightStyle = HighlightStyle.None,
    )

    @Test
    fun `tooltip shows with Custom highlight`() = runHighlightStyleTest(
        highlightStyle = HighlightStyle.Custom { _, _ -> },
    )

    @Test
    fun `step-level highlightStyle overrides host-level`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = HighlightStyle.None, // step overrides host
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                highlightStyle = HighlightStyle.Spotlight(), // host default
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "target"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Tooltip is visible (no highlight, but tooltip still shows)
        onNodeWithTag("tooltip").assertIsDisplayed()
        // Tour is active
        assertTrue(state.isActive)
    }

    @Test
    fun `host-level highlightStyle used when step uses default`() = runComposeUiTest {
        // Step uses HighlightStyle.Default → should resolve to host's Pulse
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "target"), // default highlightStyle
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                highlightStyle = HighlightStyle.Pulse(color = Color.Magenta),
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "target"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Tour renders — tooltip visible means the host dispatched to Pulse (no crash)
        onNodeWithTag("tooltip").assertIsDisplayed()
    }

    // -- Tooltip Positioning --

    @Test
    fun `tooltip does not flash at origin on first frame`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "center"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = "Tooltip Text",
                        modifier = Modifier.testTag("tooltip-position"),
                    )
                },
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "center"))
                }
            }
        }

        mainClock.autoAdvance = false

        runOnIdle { state.start() }

        // Advance frames one at a time and check that whenever the tooltip
        // appears, it is NOT near the top-left origin (y ~ 0).
        repeat(10) {
            mainClock.advanceTimeByFrame()
            waitForIdle()

            val nodes = onAllNodesWithText("Tooltip Text").fetchSemanticsNodes()
            if (nodes.isNotEmpty()) {
                val bounds = onNodeWithTag("tooltip-position").getUnclippedBoundsInRoot()
                // The target is at the center of a fillMaxSize Box, so the tooltip
                // should be positioned near the center — well below y = 0.
                // If tooltip.top is less than 100dp, it likely flashed at origin.
                assertTrue(
                    bounds.top >= 100.dp,
                    "Tooltip appeared near origin (top=${bounds.top}). " +
                        "Expected top >= 100.dp because the target is centered.",
                )
                return@runComposeUiTest
            }
        }

        // If we get here, the tooltip never appeared within 10 frames.
        // Resume auto-advance and wait for it to appear, then check position.
        mainClock.autoAdvance = true
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip Text").fetchSemanticsNodes().isNotEmpty()
        }
        val bounds = onNodeWithTag("tooltip-position").getUnclippedBoundsInRoot()
        assertTrue(
            bounds.top >= 100.dp,
            "Tooltip appeared near origin (top=${bounds.top}). " +
                "Expected top >= 100.dp because the target is centered.",
        )
    }

    // -- Multi-host (cross-hierarchy) --

    /**
     * A [WaypointOverlayHost] placed inside a [Popup] shares the outer host's
     * [WaypointState] but owns targets that live inside the popup's composition
     * tree. Each target should register against its nearest host and the
     * overlay/tooltip should render from the host that owns the current step.
     */
    @Test
    fun `targets register against the nearest host in multi-host setups`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "main"),
                WaypointStep(targetKey = "popup"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.targetKey?.toString() ?: "",
                        modifier = Modifier.testTag("tooltip-multi"),
                    )
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.size(100.dp).waypointTarget(state, "main"))
                    Popup(
                        popupPositionProvider = object : PopupPositionProvider {
                            override fun calculatePosition(
                                anchorBounds: IntRect,
                                windowSize: IntSize,
                                layoutDirection: LayoutDirection,
                                popupContentSize: IntSize,
                            ): IntOffset = IntOffset(150, 250)
                        },
                    ) {
                        WaypointOverlayHost(
                            state = state,
                            tooltipContent = { _, _ ->
                                BasicText(
                                    text = state.currentStep?.targetKey?.toString() ?: "",
                                    modifier = Modifier.testTag("tooltip-multi"),
                                )
                            },
                        ) {
                            Box(Modifier.size(100.dp).waypointTarget(state, "popup"))
                        }
                    }
                }
            }
        }

        waitForIdle()

        // Both targets registered.
        assertNotNull(state.targetCoordinates["main"], "Main target should register")
        assertNotNull(state.targetCoordinates["popup"], "Popup target should register")

        // Each target bound to its own host.
        val mainHost = state.targetHostIds["main"]
        val popupHost = state.targetHostIds["popup"]
        assertNotNull(mainHost)
        assertNotNull(popupHost)
        assertTrue(
            mainHost != popupHost,
            "Main and popup targets should be owned by distinct hosts",
        )

        // Step 0 (main) tooltip renders, belongs to the main host.
        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("main").fetchSemanticsNodes().isNotEmpty()
        }

        // Step 1 (popup) tooltip renders after advancing. Only one tooltip is
        // visible at a time since a step is owned by at most one host.
        runOnIdle { state.next() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("popup").fetchSemanticsNodes().isNotEmpty()
        }
    }

    // -- beforeShow modal transition flicker --

    /**
     * Regression: when `beforeShow` triggers a UI change that renders a NEW
     * target (e.g. a modal), there's a gap between `isStepReady` becoming true
     * and the new target registering via `onGloballyPositioned`. During that
     * gap, `targetBounds` is null, so the host falls back to `animatedBounds.value`
     * (the OLD step's bounds) — the tooltip briefly flickers at the previous
     * step's position before sliding to the new one.
     *
     * The tooltip must not render at the OLD step's position during the
     * transition. Either it stays hidden until the new target registers, or it
     * appears directly at the new position — never at the stale one.
     */
    @Test
    fun `tooltip does not appear at previous step position during beforeShow transition`() = runComposeUiTest {
        var showSecondTarget by mutableStateOf(false)

        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "first"),
                WaypointStep(
                    targetKey = "second",
                    beforeShow = { showSecondTarget = true },
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText(
                        text = state.currentStep?.targetKey?.toString() ?: "",
                        modifier = Modifier.testTag("tooltip-trans"),
                    )
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    // Step 0 target at top-left
                    Box(
                        Modifier
                            .size(80.dp)
                            .align(Alignment.TopStart)
                            .waypointTarget(state, "first"),
                    )
                    // Step 1 target at bottom-right, only mounted when
                    // beforeShow sets the flag.
                    if (showSecondTarget) {
                        Box(
                            Modifier
                                .size(80.dp)
                                .align(Alignment.BottomEnd)
                                .waypointTarget(state, "second"),
                        )
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithTag("tooltip-trans").fetchSemanticsNodes().isNotEmpty()
        }

        // Sanity check — step 0 tooltip is near the top-left target.
        val step0Bounds = onNodeWithTag("tooltip-trans").getUnclippedBoundsInRoot()
        assertTrue(
            step0Bounds.top < 200.dp,
            "Precondition: step 0 tooltip should be near top-left " +
                "(got top=${step0Bounds.top}).",
        )

        // Drive frame-by-frame to observe the transition gap.
        mainClock.autoAdvance = false
        runOnIdle { state.next() }

        // Walk frames. Any time the tooltip is visible, it must not be at the
        // old step's position. Step 0 is at (0, 0); its tooltip sits around
        // top=80dp for default Bottom placement. Step 1 is at the bottom-right
        // so its tooltip sits in the lower half of the screen (top >= ~300dp
        // in a typical test window).
        repeat(15) {
            mainClock.advanceTimeByFrame()
            waitForIdle()
            val nodes = onAllNodesWithTag("tooltip-trans").fetchSemanticsNodes()
            if (nodes.isNotEmpty()) {
                val bounds = onNodeWithTag("tooltip-trans").getUnclippedBoundsInRoot()
                // If tooltip top is < 150dp, it's rendering at step 0's spot.
                val isAtStep0Position = bounds.top < 150.dp
                assertFalse(
                    isAtStep0Position,
                    "Tooltip flickered at previous step position " +
                        "(top=${bounds.top}) during beforeShow transition. " +
                        "Expected either hidden or at step 1 position (bottom-right).",
                )
            }
        }

        mainClock.autoAdvance = true
        waitForIdle()
    }
}
