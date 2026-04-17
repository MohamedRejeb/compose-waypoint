package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test

/**
 * Screenshot tests for [SpotlightEffect] variants.
 *
 * Captures the SpotlightOverlay with each effect type and compares against
 * golden images. First run records goldens; delete a golden to re-record.
 */
@OptIn(ExperimentalTestApi::class)
class SpotlightEffectScreenshotTest {

    private val containerWidth = 400.dp
    private val containerHeight = 300.dp
    private val targetBounds = Rect(150f, 100f, 250f, 200f)

    private fun runEffectTest(
        name: String,
        effect: SpotlightEffect,
        shape: SpotlightShape = SpotlightShape.RoundedRect(cornerRadius = 12.dp),
    ) = runComposeUiTest {
        setContent {
            Box(
                modifier = Modifier
                    .size(containerWidth, containerHeight)
                    .background(Color.White)
                    .testTag("screenshot"),
            ) {
                SpotlightOverlay(
                    targetBounds = targetBounds,
                    additionalBounds = emptyList(),
                    style = HighlightStyle.Spotlight(
                        shape = shape,
                        overlayColor = Color.Black,
                        overlayAlpha = 0.6f,
                        effect = effect,
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
        ScreenshotTestHelper.assertMatchesGolden(name, image)
    }

    @Test
    fun `spotlight effect - glow`() = runEffectTest(
        name = "spotlight-effect-glow",
        effect = SpotlightEffect.Glow(
            color = Color(0xFFFFC107),
            radius = 32.dp,
            alpha = 0.7f,
        ),
    )

    @Test
    fun `spotlight effect - soft edge`() = runEffectTest(
        name = "spotlight-effect-soft-edge",
        effect = SpotlightEffect.SoftEdge(fadeWidth = 20.dp),
    )

    @Test
    fun `spotlight effect - custom red ring`() = runEffectTest(
        name = "spotlight-effect-custom-ring",
        effect = SpotlightEffect.Custom { bounds ->
            drawRect(
                color = Color.Red,
                topLeft = bounds.topLeft,
                size = bounds.size,
                style = Stroke(width = 3f),
            )
        },
    )
}
