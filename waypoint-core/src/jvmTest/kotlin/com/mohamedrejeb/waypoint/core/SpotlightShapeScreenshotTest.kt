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
 * Screenshot tests for spotlight overlay shapes.
 *
 * Captures the SpotlightOverlay with each shape variant and compares
 * against golden images. Goldens stored in src/jvmTest/resources/goldens/.
 *
 * First run records goldens. Delete a golden to re-record it.
 */
@OptIn(ExperimentalTestApi::class)
class SpotlightShapeScreenshotTest {

    // Fixed dimensions for deterministic rendering
    private val containerWidth = 400.dp
    private val containerHeight = 300.dp

    // Target centered in the container (in px, assuming 1x density in tests)
    private val targetBounds = Rect(150f, 100f, 250f, 200f)

    private fun runSpotlightTest(
        name: String,
        shape: SpotlightShape,
        padding: SpotlightPadding = SpotlightPadding.Default,
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
                    style = HighlightStyle.Spotlight(
                        shape = shape,
                        padding = padding,
                        overlayColor = Color.Black,
                        overlayAlpha = 0.6f,
                    ),
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
    fun `spotlight shape - circle`() = runSpotlightTest(
        name = "spotlight-circle",
        shape = SpotlightShape.Circle,
    )

    @Test
    fun `spotlight shape - rect`() = runSpotlightTest(
        name = "spotlight-rect",
        shape = SpotlightShape.Rect,
    )

    @Test
    fun `spotlight shape - rounded rect`() = runSpotlightTest(
        name = "spotlight-rounded-rect",
        shape = SpotlightShape.RoundedRect(cornerRadius = 12.dp),
    )

    @Test
    fun `spotlight shape - rounded rect large radius`() = runSpotlightTest(
        name = "spotlight-rounded-rect-large",
        shape = SpotlightShape.RoundedRect(cornerRadius = 24.dp),
    )

    @Test
    fun `spotlight shape - pill`() = runSpotlightTest(
        name = "spotlight-pill",
        shape = SpotlightShape.Pill,
    )

    @Test
    fun `spotlight with custom padding`() = runSpotlightTest(
        name = "spotlight-custom-padding",
        shape = SpotlightShape.RoundedRect(cornerRadius = 8.dp),
        padding = SpotlightPadding(horizontal = 16.dp, vertical = 8.dp),
    )

    @Test
    fun `spotlight with no padding`() = runSpotlightTest(
        name = "spotlight-no-padding",
        shape = SpotlightShape.Circle,
        padding = SpotlightPadding.None,
    )
}
