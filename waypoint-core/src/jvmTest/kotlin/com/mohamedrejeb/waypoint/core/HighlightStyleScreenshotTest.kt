package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test

/**
 * Screenshot tests for non-Spotlight highlight styles: Border, Pulse, Ripple.
 *
 * Pulse and Ripple use infinite animations, so we freeze the clock and
 * advance to a deterministic frame before capturing.
 */
@OptIn(ExperimentalTestApi::class)
class HighlightStyleScreenshotTest {

    private val containerWidth = 400.dp
    private val containerHeight = 300.dp
    private val targetBounds = Rect(150f, 100f, 250f, 200f)

    // -- Border (static, no animation) --

    @Test
    fun `highlight style - border circle`() = runComposeUiTest {
        setContent {
            Box(
                modifier = Modifier
                    .size(containerWidth, containerHeight)
                    .background(Color.White)
                    .testTag("screenshot"),
            ) {
                BorderHighlight(
                    targetBounds = targetBounds,
                    style = HighlightStyle.Border(
                        color = Color.Red,
                        shape = SpotlightShape.Circle,
                        borderWidth = 3.dp,
                    ),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        waitForIdle()
        val image = onNodeWithTag("screenshot").captureToImage()
        ScreenshotTestHelper.assertMatchesGolden("highlight-border-circle", image)
    }

    @Test
    fun `highlight style - border rounded rect`() = runComposeUiTest {
        setContent {
            Box(
                modifier = Modifier
                    .size(containerWidth, containerHeight)
                    .background(Color.White)
                    .testTag("screenshot"),
            ) {
                BorderHighlight(
                    targetBounds = targetBounds,
                    style = HighlightStyle.Border(
                        color = Color(0xFFFF5722),
                        shape = SpotlightShape.RoundedRect(cornerRadius = 12.dp),
                        borderWidth = 2.dp,
                    ),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        waitForIdle()
        val image = onNodeWithTag("screenshot").captureToImage()
        ScreenshotTestHelper.assertMatchesGolden("highlight-border-rounded-rect", image)
    }

    // -- Pulse (animated — freeze clock at a specific frame) --

    @Test
    fun `highlight style - pulse circle`() = runComposeUiTest {
        mainClock.autoAdvance = false

        setContent {
            Box(
                modifier = Modifier
                    .size(containerWidth, containerHeight)
                    .background(Color.White)
                    .testTag("screenshot"),
            ) {
                PulseHighlight(
                    targetBounds = targetBounds,
                    style = HighlightStyle.Pulse(
                        color = Color(0xFF6200EE),
                        shape = SpotlightShape.Circle,
                        borderWidth = 3.dp,
                        pulseScale = 1.15f,
                        durationMillis = 1200,
                    ),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // Advance to ~50% of the pulse animation (300ms into 600ms half-cycle)
        mainClock.advanceTimeBy(300)

        val image = onNodeWithTag("screenshot").captureToImage()
        ScreenshotTestHelper.assertMatchesGolden("highlight-pulse-circle", image)
    }

    @Test
    fun `highlight style - pulse rounded rect`() = runComposeUiTest {
        mainClock.autoAdvance = false

        setContent {
            Box(
                modifier = Modifier
                    .size(containerWidth, containerHeight)
                    .background(Color.White)
                    .testTag("screenshot"),
            ) {
                PulseHighlight(
                    targetBounds = targetBounds,
                    style = HighlightStyle.Pulse(
                        color = Color(0xFF03DAC5),
                        shape = SpotlightShape.RoundedRect(cornerRadius = 12.dp),
                        borderWidth = 3.dp,
                    ),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        mainClock.advanceTimeBy(300)

        val image = onNodeWithTag("screenshot").captureToImage()
        ScreenshotTestHelper.assertMatchesGolden("highlight-pulse-rounded-rect", image)
    }

    // -- Ripple (animated — freeze clock at a specific frame) --

    @Test
    fun `highlight style - ripple`() = runComposeUiTest {
        mainClock.autoAdvance = false

        setContent {
            Box(
                modifier = Modifier
                    .size(containerWidth, containerHeight)
                    .background(Color.White)
                    .testTag("screenshot"),
            ) {
                RippleHighlight(
                    targetBounds = targetBounds,
                    style = HighlightStyle.Ripple(
                        color = Color(0xFF03DAC5),
                        ringCount = 3,
                        durationMillis = 2000,
                        maxRadius = 60.dp,
                    ),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // Advance to 500ms — rings are spread out at different radii
        mainClock.advanceTimeBy(500)

        val image = onNodeWithTag("screenshot").captureToImage()
        ScreenshotTestHelper.assertMatchesGolden("highlight-ripple", image)
    }

    @Test
    fun `highlight style - ripple custom color`() = runComposeUiTest {
        mainClock.autoAdvance = false

        setContent {
            Box(
                modifier = Modifier
                    .size(containerWidth, containerHeight)
                    .background(Color.White)
                    .testTag("screenshot"),
            ) {
                RippleHighlight(
                    targetBounds = targetBounds,
                    style = HighlightStyle.Ripple(
                        color = Color(0xFFFF5722),
                        ringCount = 4,
                        durationMillis = 1500,
                        maxRadius = 80.dp,
                    ),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        mainClock.advanceTimeBy(400)

        val image = onNodeWithTag("screenshot").captureToImage()
        ScreenshotTestHelper.assertMatchesGolden("highlight-ripple-custom", image)
    }
}
