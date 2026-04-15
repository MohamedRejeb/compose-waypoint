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
 * Screenshot tests for multi-element highlighting.
 * Verifies that multiple cutouts/highlights render correctly for
 * Spotlight, Border, and Pulse styles.
 */
@OptIn(ExperimentalTestApi::class)
class MultiElementScreenshotTest {

    private val containerWidth = 400.dp
    private val containerHeight = 300.dp

    // Three targets spread across the container
    private val primaryBounds = Rect(30f, 100f, 100f, 170f)
    private val secondaryBounds = Rect(160f, 100f, 230f, 170f)
    private val tertiaryBounds = Rect(290f, 100f, 370f, 170f)

    private val additionalBounds = listOf(secondaryBounds, tertiaryBounds)

    // -- Spotlight with multiple cutouts --

    @Test
    fun `multi-element spotlight - 3 cutouts`() = runComposeUiTest {
        setContent {
            Box(
                modifier = Modifier
                    .size(containerWidth, containerHeight)
                    .background(Color.White)
                    .testTag("screenshot"),
            ) {
                SpotlightOverlay(
                    targetBounds = primaryBounds,
                    additionalBounds = additionalBounds,
                    style = HighlightStyle.Spotlight(
                        shape = SpotlightShape.RoundedRect(cornerRadius = 8.dp),
                        overlayColor = Color.Black,
                        overlayAlpha = 0.6f,
                    ),
                    allowTargetInteraction = false,
                    onOverlayClick = {},
                    onTargetClick = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        waitForIdle()
        val image = onNodeWithTag("screenshot").captureToImage()
        ScreenshotTestHelper.assertMatchesGolden("multi-spotlight-3-cutouts", image)
    }

    @Test
    fun `multi-element spotlight - circle shape`() = runComposeUiTest {
        setContent {
            Box(
                modifier = Modifier
                    .size(containerWidth, containerHeight)
                    .background(Color.White)
                    .testTag("screenshot"),
            ) {
                SpotlightOverlay(
                    targetBounds = primaryBounds,
                    additionalBounds = additionalBounds,
                    style = HighlightStyle.Spotlight(
                        shape = SpotlightShape.Circle,
                        overlayColor = Color.Black,
                        overlayAlpha = 0.6f,
                    ),
                    allowTargetInteraction = false,
                    onOverlayClick = {},
                    onTargetClick = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        waitForIdle()
        val image = onNodeWithTag("screenshot").captureToImage()
        ScreenshotTestHelper.assertMatchesGolden("multi-spotlight-circle", image)
    }

    // -- Border with multiple targets --

    @Test
    fun `multi-element border - 3 targets`() = runComposeUiTest {
        setContent {
            Box(
                modifier = Modifier
                    .size(containerWidth, containerHeight)
                    .background(Color.White)
                    .testTag("screenshot"),
            ) {
                BorderHighlight(
                    targetBounds = primaryBounds,
                    additionalBounds = additionalBounds,
                    style = HighlightStyle.Border(
                        color = Color(0xFFFF5722),
                        shape = SpotlightShape.RoundedRect(cornerRadius = 8.dp),
                        borderWidth = 2.dp,
                    ),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        waitForIdle()
        val image = onNodeWithTag("screenshot").captureToImage()
        ScreenshotTestHelper.assertMatchesGolden("multi-border-3-targets", image)
    }

    // -- Pulse with multiple targets --

    @Test
    fun `multi-element pulse - 3 targets`() = runComposeUiTest {
        mainClock.autoAdvance = false

        setContent {
            Box(
                modifier = Modifier
                    .size(containerWidth, containerHeight)
                    .background(Color.White)
                    .testTag("screenshot"),
            ) {
                PulseHighlight(
                    targetBounds = primaryBounds,
                    additionalBounds = additionalBounds,
                    style = HighlightStyle.Pulse(
                        color = Color(0xFF6200EE),
                        shape = SpotlightShape.RoundedRect(cornerRadius = 8.dp),
                        borderWidth = 3.dp,
                    ),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        mainClock.advanceTimeBy(300)

        val image = onNodeWithTag("screenshot").captureToImage()
        ScreenshotTestHelper.assertMatchesGolden("multi-pulse-3-targets", image)
    }

    // -- Ripple with multiple targets --

    @Test
    fun `multi-element ripple - 3 targets`() = runComposeUiTest {
        mainClock.autoAdvance = false

        setContent {
            Box(
                modifier = Modifier
                    .size(containerWidth, containerHeight)
                    .background(Color.White)
                    .testTag("screenshot"),
            ) {
                RippleHighlight(
                    targetBounds = primaryBounds,
                    additionalBounds = additionalBounds,
                    style = HighlightStyle.Ripple(
                        color = Color(0xFF03DAC5),
                        ringCount = 3,
                        maxRadius = 50.dp,
                    ),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        mainClock.advanceTimeBy(500)

        val image = onNodeWithTag("screenshot").captureToImage()
        ScreenshotTestHelper.assertMatchesGolden("multi-ripple-3-targets", image)
    }
}
