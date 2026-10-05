package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertTrue

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
    private val defaultTargetBounds = Rect(150f, 100f, 250f, 200f)

    private fun runEffectTest(
        name: String,
        effect: SpotlightEffect,
        shape: SpotlightShape = SpotlightShape.RoundedRect(cornerRadius = 12.dp),
        bounds: Rect = defaultTargetBounds,
    ) = runComposeUiTest {
        setContent {
            Box(
                modifier = Modifier
                    .size(containerWidth, containerHeight)
                    .background(Color.White)
                    .testTag("screenshot"),
            ) {
                SpotlightOverlay(
                    targetBounds = { listOf(bounds) },
                    style = HighlightStyle.Spotlight(
                        shape = shape,
                        overlayColor = Color.Black,
                        overlayAlpha = 0.6f,
                        effect = effect,
                    ),
                    passThrough = false,
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
        effect = SpotlightEffect.SoftEdge(fadeWidth = 36.dp),
        shape = SpotlightShape.Pill,
        bounds = Rect(110f, 120f, 290f, 180f),
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

    @Test
    fun `spotlight effect - soft edge produces gradient pixels`() = runComposeUiTest {
        val boxSizeDp = 300.dp
        val cutoutRadiusPx = 60f
        val fadeWidthDp = 30.dp
        // Center the cutout in the 300x300 container.
        val cutoutBounds = Rect(
            left = 150f - cutoutRadiusPx,
            top = 150f - cutoutRadiusPx,
            right = 150f + cutoutRadiusPx,
            bottom = 150f + cutoutRadiusPx,
        )

        setContent {
            Box(
                modifier = Modifier
                    .size(boxSizeDp, boxSizeDp)
                    .background(Color.White)
                    .testTag("softedge-pixel"),
            ) {
                SpotlightOverlay(
                    targetBounds = { listOf(cutoutBounds) },
                    style = HighlightStyle.Spotlight(
                        shape = SpotlightShape.Circle,
                        overlayColor = Color.Black,
                        overlayAlpha = 0.6f,
                        effect = SpotlightEffect.SoftEdge(fadeWidth = fadeWidthDp),
                    ),
                    passThrough = false,
                    onOverlayClick = {},
                    onTargetClick = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        waitForIdle()

        val image = onNodeWithTag("softedge-pixel").captureToImage()
        val pixelMap = image.toPixelMap()

        // 1. Center of cutout -> white background (fully transparent overlay).
        val centerColor = pixelMap[image.width / 2, image.height / 2]
        val centerR = (centerColor.red * 255).toInt()
        val centerG = (centerColor.green * 255).toInt()
        val centerB = (centerColor.blue * 255).toInt()
        val centerBrightness = (centerR + centerG + centerB) / 3
        assertTrue(
            centerR >= 245 && centerG >= 245 && centerB >= 245,
            "Cutout center should be near-white (R=$centerR, G=$centerG, B=$centerB)",
        )

        // 2. Far corner -> solid scrim (0.6 black on white = RGB ~102).
        val cornerColor = pixelMap[5, 5]
        val cornerR = (cornerColor.red * 255).toInt()
        val cornerG = (cornerColor.green * 255).toInt()
        val cornerB = (cornerColor.blue * 255).toInt()
        val scrimBrightness = (cornerR + cornerG + cornerB) / 3
        assertTrue(
            scrimBrightness in 92..112,
            "Far-corner scrim should be ~gray 102 (brightness=$scrimBrightness, RGB=$cornerR/$cornerG/$cornerB)",
        )

        // 3. Feather band pixel -> 15px beyond the cutout edge (halfway through
        // a 30dp fade; density is 1.0 in runComposeUiTest). Should be strictly
        // between solid scrim and full white -> asserts real gradient, not hard edge.
        val featherX = image.width / 2
        val featherY = (image.height / 2) + cutoutRadiusPx.toInt() + 15
        val featherColor = pixelMap[featherX, featherY]
        val featherR = (featherColor.red * 255).toInt()
        val featherG = (featherColor.green * 255).toInt()
        val featherB = (featherColor.blue * 255).toInt()
        val featherBrightness = (featherR + featherG + featherB) / 3

        assertTrue(
            featherBrightness > scrimBrightness + 10,
            "Feather pixel should be brighter than solid scrim: " +
                "feather=$featherBrightness, scrim=$scrimBrightness (diff too small -> gradient missing)",
        )
        assertTrue(
            featherBrightness < 245,
            "Feather pixel should be dimmer than full white: feather=$featherBrightness " +
                "(no feather -> SoftEdge behaves like None)",
        )
    }

    @Test
    fun `spotlight effect - soft edge follows pill shape`() = runComposeUiTest {
        // Pill target 180x60 centered in a 400x300 box at (200, 150).
        // Horizontal span: x=[110, 290], vertical span: y=[120, 180].
        val pillBounds = Rect(left = 110f, top = 120f, right = 290f, bottom = 180f)
        val fadeWidthDp = 24.dp

        setContent {
            Box(
                modifier = Modifier
                    .size(containerWidth, containerHeight)
                    .background(Color.White)
                    .testTag("softedge-pill"),
            ) {
                SpotlightOverlay(
                    targetBounds = { listOf(pillBounds) },
                    style = HighlightStyle.Spotlight(
                        shape = SpotlightShape.Pill,
                        overlayColor = Color.Black,
                        overlayAlpha = 0.6f,
                        effect = SpotlightEffect.SoftEdge(fadeWidth = fadeWidthDp),
                    ),
                    passThrough = false,
                    onOverlayClick = {},
                    onTargetClick = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        waitForIdle()

        val image = onNodeWithTag("softedge-pill").captureToImage()
        val pixelMap = image.toPixelMap()

        fun brightnessAt(x: Int, y: Int): Int {
            val c = pixelMap[x, y]
            val r = (c.red * 255).toInt()
            val g = (c.green * 255).toInt()
            val b = (c.blue * 255).toInt()
            return (r + g + b) / 3
        }

        // Sample A: (200, 85) — 35px above the pill's top edge (feather is 24dp).
        // Outside the feather band -> near-full scrim (~102 brightness).
        val brightnessA = brightnessAt(200, 85)

        // Sample B: (200, 100) — 20px above the pill's top edge, within the
        // feather band -> partially cleared, brighter than A.
        val brightnessB = brightnessAt(200, 100)

        // Sample C: (70, 150) — 40px to the left of the pill's left edge.
        // Radial distance from pill center (200, 150) is 130px, which a
        // circular feather implementation would (wrongly) cover. A shape-aware
        // feather leaves this pixel at near-full scrim.
        val brightnessC = brightnessAt(70, 150)

        assertTrue(
            brightnessA in 77..127,
            "Sample A (200, 85) should be near full scrim (~102). Got $brightnessA.",
        )
        assertTrue(
            brightnessB > brightnessA + 10,
            "Sample B (200, 100) should be inside the feather band and brighter " +
                "than A. A=$brightnessA, B=$brightnessB.",
        )
        assertTrue(
            brightnessC in 77..127,
            "Sample C (70, 150) should be near full scrim because it's far from " +
                "the pill outline (shape-aware feather). Got $brightnessC.",
        )
        // Shape-fidelity: C must be dramatically closer to scrim than B, even
        // though C is on the pill's long axis. A circular feather would make
        // C closer to B than to A.
        assertTrue(
            kotlin.math.abs(brightnessC - brightnessA) < kotlin.math.abs(brightnessC - brightnessB),
            "Feather should follow pill shape, not a circle. " +
                "A=$brightnessA, B=$brightnessB, C=$brightnessC " +
                "(C should be close to A, far from B).",
        )
    }
}
