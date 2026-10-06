package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The glow is a halo around the cutout: it follows the cutout's shape, stays
 * within its radius, and never paints over the target.
 *
 * Scene: a wide 300x60dp target centered on white, a red glow of 24dp at full
 * alpha, no scrim and no padding, so every red pixel is glow.
 */
@OptIn(ExperimentalTestApi::class)
class GlowShapeUiTest {

    private val targetWidth = 300.dp
    private val targetHeight = 60.dp
    private val glowRadius = 24.dp

    private class Scene(private val pixels: PixelMap, private val density: Float) {
        private fun px(dp: Dp): Int = (dp.value * density).toInt()

        /** Whether the pixel at an offset from the scene's center is tinted by the glow. */
        fun isGlowAt(dx: Dp, dy: Dp): Boolean {
            val color = pixels[pixels.width / 2 + px(dx), pixels.height / 2 + px(dy)]
            return color.red > 0.6f && color.green < 0.8f
        }
    }

    private fun runGlowScene(shape: SpotlightShape, block: Scene.() -> Unit) = runComposeUiTest {
        val state = WaypointState(steps = listOf(WaypointStep(targetKey = "wide")))
        setContent {
            WaypointHost(
                state = state,
                modifier = Modifier.testTag("scene"),
                highlightStyle = HighlightStyle.Spotlight(
                    shape = shape,
                    padding = SpotlightPadding.None,
                    overlayAlpha = 0f,
                    effect = SpotlightEffect.Glow(color = Color.Red, radius = glowRadius, alpha = 1f),
                ),
                tooltipContent = { _ -> BasicText("Tooltip") },
            ) {
                Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(targetWidth, targetHeight).waypointTarget(state, "wide"))
                }
            }
        }
        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) { onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty() }
        waitForIdle()
        Scene(onNodeWithTag("scene").captureToImage().toPixelMap(), density.density).block()
    }

    @Test
    fun `glow does not paint over a wide target`() = runGlowScene(SpotlightShape.Rect) {
        assertFalse(isGlowAt(dx = 0.dp, dy = 0.dp), "center of the target")
        assertFalse(isGlowAt(dx = (-100).dp, dy = 0.dp), "inside the target, towards its left edge")
        assertFalse(isGlowAt(dx = 140.dp, dy = 20.dp), "inside the target, near its corner")
    }

    @Test
    fun `glow hugs every edge of a wide target`() = runGlowScene(SpotlightShape.Rect) {
        assertTrue(isGlowAt(dx = (-154).dp, dy = 0.dp), "just left of the target")
        assertTrue(isGlowAt(dx = 154.dp, dy = 0.dp), "just right of the target")
        assertTrue(isGlowAt(dx = 130.dp, dy = (-34).dp), "just above the target, far from its center")
    }

    @Test
    fun `glow ends at its radius`() = runGlowScene(SpotlightShape.Rect) {
        // 60dp above the top edge, well past the 24dp radius.
        assertFalse(isGlowAt(dx = 0.dp, dy = (-90).dp), "far above the target")
        assertFalse(isGlowAt(dx = (-150 - 40).dp, dy = 0.dp), "far left of the target")
    }

    @Test
    fun `glow follows a circle cutout`() = runGlowScene(SpotlightShape.Circle) {
        // The circle cutout has a 150dp radius around the wide target.
        assertFalse(isGlowAt(dx = 0.dp, dy = (-100).dp), "inside the circle, above the target")
        assertTrue(isGlowAt(dx = 0.dp, dy = (-154).dp), "just outside the circle")
    }
}
