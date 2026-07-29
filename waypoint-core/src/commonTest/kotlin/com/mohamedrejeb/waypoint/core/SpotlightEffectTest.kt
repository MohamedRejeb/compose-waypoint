package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for Tier 3.6 Spotlight Effects.
 *
 * Covers the sealed [SpotlightEffect] hierarchy (None, Glow, SoftEdge, Custom)
 * and the [HighlightStyle.Spotlight.effect] property.
 */
@OptIn(ExperimentalTestApi::class)
class SpotlightEffectTest {

    // -- Pure unit tests: default value --

    @Test
    fun `default spotlight has no effect`() {
        val style = HighlightStyle.Spotlight()

        assertEquals(SpotlightEffect.None, style.effect)
    }

    @Test
    fun `default highlight style is Spotlight with None effect`() {
        val default = WaypointDefaults.HighlightStyle

        assertTrue(default is HighlightStyle.Spotlight)
        assertEquals(SpotlightEffect.None, default.effect)
    }

    // -- Pure unit tests: data class equality --

    @Test
    fun `two Glow effects with same fields are equal`() {
        val a = SpotlightEffect.Glow(Color.Red, 24.dp, 0.5f)
        val b = SpotlightEffect.Glow(Color.Red, 24.dp, 0.5f)

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `Glow effects with different color are not equal`() {
        val a = SpotlightEffect.Glow(Color.Red, 24.dp, 0.5f)
        val b = SpotlightEffect.Glow(Color.Blue, 24.dp, 0.5f)

        assertNotEquals(a, b)
    }

    @Test
    fun `Glow effects with different radius are not equal`() {
        val a = SpotlightEffect.Glow(Color.Red, 24.dp, 0.5f)
        val b = SpotlightEffect.Glow(Color.Red, 32.dp, 0.5f)

        assertNotEquals(a, b)
    }

    @Test
    fun `Glow effects with different alpha are not equal`() {
        val a = SpotlightEffect.Glow(Color.Red, 24.dp, 0.5f)
        val b = SpotlightEffect.Glow(Color.Red, 24.dp, 0.7f)

        assertNotEquals(a, b)
    }

    @Test
    fun `Glow has default color White, radius 24dp, alpha 0_6`() {
        val glow = SpotlightEffect.Glow()

        assertEquals(Color.White, glow.color)
        assertEquals(24.dp, glow.radius)
        assertEquals(0.6f, glow.alpha)
    }

    @Test
    fun `two SoftEdge effects with same fadeWidth are equal`() {
        val a = SpotlightEffect.SoftEdge(16.dp)
        val b = SpotlightEffect.SoftEdge(16.dp)

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `SoftEdge effects with different fadeWidth are not equal`() {
        val a = SpotlightEffect.SoftEdge(16.dp)
        val b = SpotlightEffect.SoftEdge(24.dp)

        assertNotEquals(a, b)
    }

    @Test
    fun `SoftEdge has default fadeWidth 16dp`() {
        val softEdge = SpotlightEffect.SoftEdge()

        assertEquals(16.dp, softEdge.fadeWidth)
    }

    @Test
    fun `None is a data object with single instance`() {
        val a: SpotlightEffect = SpotlightEffect.None
        val b: SpotlightEffect = SpotlightEffect.None

        // data object: referential identity
        assertSame(a, b)
        assertEquals(a, b)
    }

    @Test
    fun `Spotlight with Glow differs from default Spotlight`() {
        val withGlow = HighlightStyle.Spotlight(
            effect = SpotlightEffect.Glow(Color.Red, 24.dp, 0.5f),
        )
        val noEffect = HighlightStyle.Spotlight()

        assertNotEquals(noEffect, withGlow)
        assertNotEquals(noEffect.effect, withGlow.effect)
    }

    @Test
    fun `Spotlight copy with different effect is not equal to original`() {
        val base = HighlightStyle.Spotlight()
        val copied = base.copy(effect = SpotlightEffect.SoftEdge(8.dp))

        assertNotEquals(base, copied)
        assertEquals(SpotlightEffect.None, base.effect)
        assertEquals(SpotlightEffect.SoftEdge(8.dp), copied.effect)
    }

    // -- UI tests: rendering doesn't crash with various effects --

    @Test
    fun `tour with None effect renders and shows tooltip`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = HighlightStyle.Spotlight(effect = SpotlightEffect.None),
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "target"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tip").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("tooltip").assertIsDisplayed()
        assertTrue(state.isActive)
    }

    @Test
    fun `Glow effect with small target bounds does not crash`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "tiny",
                    highlightStyle = HighlightStyle.Spotlight(
                        effect = SpotlightEffect.Glow(
                            color = Color.Red,
                            radius = 32.dp,
                            alpha = 0.7f,
                        ),
                    ),
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    // Very small target: 20dp x 20dp
                    Box(Modifier.size(20.dp).waypointTarget(state, "tiny"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tip").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("tooltip").assertIsDisplayed()
        assertTrue(state.isActive)
    }

    @Test
    fun `SoftEdge with zero fadeWidth does not crash`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = HighlightStyle.Spotlight(
                        effect = SpotlightEffect.SoftEdge(fadeWidth = 0.dp),
                    ),
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "target"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tip").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("tooltip").assertIsDisplayed()
        assertTrue(state.isActive)
    }

    @Test
    fun `SoftEdge with non-zero fadeWidth does not crash`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = HighlightStyle.Spotlight(
                        effect = SpotlightEffect.SoftEdge(fadeWidth = 24.dp),
                    ),
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "target"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tip").fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithTag("tooltip").assertIsDisplayed()
    }

    // -- UI test: Custom lambda is invoked per cutout --

    @Test
    fun `Custom effect draw lambda is invoked at least once per target`() = runComposeUiTest {
        var invocationCount = 0
        val customEffect = SpotlightEffect.Custom { _ ->
            invocationCount++
        }

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "primary",
                    additionalTargets = listOf("extra1", "extra2"),
                    highlightStyle = HighlightStyle.Spotlight(effect = customEffect),
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Row {
                        Box(Modifier.size(50.dp).waypointTarget(state, "primary"))
                        Spacer(Modifier.width(10.dp))
                        Box(Modifier.size(50.dp).waypointTarget(state, "extra1"))
                        Spacer(Modifier.width(10.dp))
                        Box(Modifier.size(50.dp).waypointTarget(state, "extra2"))
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tip").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()

        // Lambda runs inside DrawScope for primary + 2 additional targets = 3 per draw pass.
        // Compose may draw multiple times (animation frames, popup layouts), so we only
        // assert at least 3 invocations to avoid flakiness.
        assertTrue(
            invocationCount >= 3,
            "Expected Custom effect lambda to fire at least 3 times (1 primary + 2 additional). " +
                "Actual: $invocationCount",
        )
    }

    @Test
    fun `Custom effect draw lambda fires for single target`() = runComposeUiTest {
        var invocationCount = 0
        val customEffect = SpotlightEffect.Custom { _ ->
            invocationCount++
        }

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "solo",
                    highlightStyle = HighlightStyle.Spotlight(effect = customEffect),
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "solo"))
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tip").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()

        assertTrue(
            invocationCount >= 1,
            "Expected Custom effect lambda to fire at least once. Actual: $invocationCount",
        )
    }

    @Test
    fun `Custom effect does not fire when tour is inactive`() = runComposeUiTest {
        var invocationCount = 0
        val customEffect = SpotlightEffect.Custom { _ ->
            invocationCount++
        }

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = HighlightStyle.Spotlight(effect = customEffect),
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(Modifier.fillMaxSize()) {
                    Box(Modifier.size(60.dp).waypointTarget(state, "target"))
                }
            }
        }

        // Never call state.start() — the overlay should not render
        waitForIdle()

        assertEquals(
            0, invocationCount,
            "Custom effect lambda must not fire while the tour is inactive",
        )
    }
}
