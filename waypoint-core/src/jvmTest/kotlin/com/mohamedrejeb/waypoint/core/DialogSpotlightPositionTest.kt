package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests that the spotlight cutout is positioned correctly when
 * WaypointHost is inside a Dialog.
 *
 * The Dialog is a Popup-based container that may offset its content
 * from the window root. If `boundsInRoot()` returns window-relative
 * coordinates but the Canvas draws in dialog-relative space, the
 * cutout will appear at the wrong position.
 *
 * These tests use pixel sampling to verify the cutout is at the target,
 * not offset by the Dialog's position.
 */
@OptIn(ExperimentalTestApi::class)
class DialogSpotlightPositionTest {

    // Fixed: bounds now computed relative to WaypointHost via localBoundingBoxOf
    @Test
    fun `spotlight cutout aligns with target inside Dialog`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = HighlightStyle.Spotlight(
                        shape = SpotlightShape.RoundedRect(cornerRadius = 8.dp),
                        overlayColor = Color.Black,
                        overlayAlpha = 0.7f,
                    ),
                ),
            ),
        )

        setContent {
            // The dialog adds padding/centering which offsets content from root
            Dialog(onDismissRequest = {}) {
                WaypointHost(
                    state = state,
                    tooltipContent = { _, _ ->
                        BasicText("Tooltip", Modifier.testTag("tooltip"))
                    },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color.White)
                            .testTag("dialog-content"),
                    ) {
                        // Target placed at center of dialog content
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(80.dp)
                                .background(Color.White)
                                .waypointTarget(state, "target")
                                .testTag("target"),
                        )
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        // Capture the dialog content area
        val image = onNodeWithTag("dialog-content").captureToImage()
        val pixelMap = image.toPixelMap()

        // The target is centered in the dialog content.
        // At the center of the image, the spotlight cutout should reveal white.
        val centerX = image.width / 2
        val centerY = image.height / 2

        val pixelAtCenter = pixelMap[centerX, centerY]
        val centerBrightness = (pixelAtCenter.red + pixelAtCenter.green + pixelAtCenter.blue) / 3f

        // A corner of the image should be covered by the dark overlay
        val cornerPixel = pixelMap[5, 5]
        val cornerBrightness = (cornerPixel.red + cornerPixel.green + cornerPixel.blue) / 3f

        assertTrue(
            centerBrightness > 0.7f,
            buildString {
                appendLine("Spotlight cutout is NOT at the target center inside Dialog!")
                appendLine("Center pixel ($centerX, $centerY): $pixelAtCenter, brightness: ${"%.2f".format(centerBrightness)}")
                appendLine("Expected brightness > 0.7 (white/cutout area)")
                appendLine()
                appendLine("This means boundsInRoot() coordinates don't match the Canvas space inside the Dialog.")
                appendLine("Fix: use boundsInParent() or account for the Dialog's offset from root.")
            },
        )

        assertTrue(
            cornerBrightness < 0.5f,
            buildString {
                appendLine("Overlay is not visible at the corner — spotlight may not be rendering at all.")
                appendLine("Corner pixel (5, 5): $cornerPixel, brightness: ${"%.2f".format(cornerBrightness)}")
                appendLine("Expected brightness < 0.5 (dark overlay)")
            },
        )
    }

    // Fixed: bounds now computed relative to WaypointHost via localBoundingBoxOf
    @Test
    fun `spotlight cutout tracks target at non-center position inside Dialog`() = runComposeUiTest {
        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "target",
                    highlightStyle = HighlightStyle.Spotlight(
                        shape = SpotlightShape.Circle,
                        overlayColor = Color.Black,
                        overlayAlpha = 0.7f,
                    ),
                ),
            ),
        )

        setContent {
            Dialog(onDismissRequest = {}) {
                WaypointHost(
                    state = state,
                    tooltipContent = { _, _ ->
                        BasicText("Tooltip", Modifier.testTag("tooltip"))
                    },
                ) {
                    // Target at top-left area, not centered
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color.White)
                            .testTag("dialog-content"),
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(start = 20.dp, top = 20.dp)
                                .size(60.dp)
                                .background(Color.White)
                                .waypointTarget(state, "target"),
                        )
                    }
                }
            }
        }

        runOnIdle { state.start() }
        waitUntil(timeoutMillis = 3000) {
            onAllNodesWithText("Tooltip").fetchSemanticsNodes().isNotEmpty()
        }

        val image = onNodeWithTag("dialog-content").captureToImage()
        val pixelMap = image.toPixelMap()

        // Target is at ~(20+30, 20+30) = (50, 50) dp from top-left of dialog content
        // In test density (1.0), that's approximately (50, 50) px
        val targetCenterX = 50.coerceIn(0, image.width - 1)
        val targetCenterY = 50.coerceIn(0, image.height - 1)

        val pixelAtTarget = pixelMap[targetCenterX, targetCenterY]
        val targetBrightness = (pixelAtTarget.red + pixelAtTarget.green + pixelAtTarget.blue) / 3f

        // Sample far from the target — should be overlay
        val farX = (image.width - 10).coerceIn(0, image.width - 1)
        val farY = (image.height - 10).coerceIn(0, image.height - 1)
        val pixelFar = pixelMap[farX, farY]
        val farBrightness = (pixelFar.red + pixelFar.green + pixelFar.blue) / 3f

        assertTrue(
            targetBrightness > 0.7f,
            buildString {
                appendLine("Spotlight cutout not at expected target position inside Dialog!")
                appendLine("Target area ($targetCenterX, $targetCenterY): $pixelAtTarget, brightness: ${"%.2f".format(targetBrightness)}")
                appendLine("Expected brightness > 0.7 (cutout/white)")
                appendLine("Image size: ${image.width}x${image.height}")
            },
        )

        assertTrue(
            farBrightness < 0.5f,
            "Overlay not visible far from target. Pixel ($farX, $farY): $pixelFar, brightness: ${"%.2f".format(farBrightness)}",
        )
    }
}
