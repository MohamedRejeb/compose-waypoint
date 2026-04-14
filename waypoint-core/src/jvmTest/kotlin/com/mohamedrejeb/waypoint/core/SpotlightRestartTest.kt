package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
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
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Tests that the spotlight snaps (not animates) to the first target
 * when restarting a tour after completion.
 *
 * Bug: After a tour completes, `animatedBounds` in WaypointHost retains
 * the last target's position. On restart, the spotlight animates FROM that
 * stale position instead of snapping to the new first target. The check
 * `animatedBounds.value == Rect.Zero` only matches on the very first activation.
 *
 * Root cause (WaypointHost.kt):
 * ```
 * if (animatedBounds.value == Rect.Zero) {
 *     animatedBounds.snapTo(targetBounds)     // only on first-ever start
 * } else {
 *     animatedBounds.animateTo(targetBounds)   // every restart — wrong!
 * }
 * ```
 * Fix: reset animatedBounds to Rect.Zero when tour becomes inactive,
 * or track isActive transitions to decide snap vs animate.
 */
@OptIn(ExperimentalTestApi::class)
class SpotlightRestartTest {

    /**
     * Detects the bug by checking spotlight cutout position immediately
     * after restarting a completed tour.
     *
     * Setup: two targets far apart. Complete tour (ends at target B).
     * Restart tour (should start at target A).
     *
     * On the first frame after restart, sample pixels at target A's center.
     * If the spotlight snapped correctly, the cutout is at A → pixels are
     * the white background (not the dark overlay).
     * If it's animating from B → cutout is still near B, and A's area is
     * covered by the overlay → dark pixels.
     */
    // Fixed: animatedBounds reset to Rect.Zero when tour becomes inactive
    @Test
    fun `spotlight snaps to first target on tour restart after completion`() = runComposeUiTest {
        mainClock.autoAdvance = false

        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "left"),
                WaypointStep(targetKey = "right"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .testTag("screenshot"),
                ) {
                    // Left target at (20, 50)
                    Box(
                        modifier = Modifier
                            .offset(x = 20.dp, y = 50.dp)
                            .size(60.dp)
                            .background(Color.White)
                            .waypointTarget(state, "left"),
                    )
                    // Right target at (300, 50) — far from left
                    Box(
                        modifier = Modifier
                            .offset(x = 300.dp, y = 50.dp)
                            .size(60.dp)
                            .background(Color.White)
                            .waypointTarget(state, "right"),
                    )
                }
            }
        }

        // Let composition settle
        mainClock.advanceTimeBy(100)

        // === First tour: start → next → complete ===
        runOnIdle { state.start() }
        mainClock.advanceTimeBy(500) // settle step 1

        runOnIdle { state.next() }
        mainClock.advanceTimeBy(500) // settle step 2 (spotlight now at "right")

        runOnIdle { state.next() } // complete tour (last step → inactive)
        mainClock.advanceTimeBy(100)

        // Verify tour is inactive
        assertTrue(!state.isActive, "Tour should be inactive after completion")

        // === Second tour: restart ===
        runOnIdle { state.start() }
        // Advance just 2 frames — if snap, cutout is already at "left"
        mainClock.advanceTimeBy(32)

        // Capture the overlay area and check pixels at the LEFT target's center
        // Left target center is approximately (50, 80) in dp → pixels depend on density
        // In test environment density is 1.0, so dp ≈ px
        val image = onNodeWithTag("screenshot").captureToImage()
        val pixelMap = image.toPixelMap()

        // Sample at the center of the left target (20+30=50, 50+30=80)
        val leftTargetCenterX = 50
        val leftTargetCenterY = 80

        // Clamp to image bounds
        val sampleX = leftTargetCenterX.coerceIn(0, image.width - 1)
        val sampleY = leftTargetCenterY.coerceIn(0, image.height - 1)

        val pixelAtLeftTarget = pixelMap[sampleX, sampleY]

        // If spotlight snapped to "left": cutout is here → pixel is white (background)
        // If spotlight is still at/near "right": overlay covers "left" → pixel is dark
        val brightness = (pixelAtLeftTarget.red + pixelAtLeftTarget.green + pixelAtLeftTarget.blue) / 3f

        assertTrue(
            brightness > 0.7f,
            buildString {
                appendLine("Spotlight did NOT snap to first target on tour restart!")
                appendLine("Pixel at left target center ($sampleX, $sampleY): $pixelAtLeftTarget")
                appendLine("Brightness: ${"%.2f".format(brightness)} (expected > 0.7 for white/cutout)")
                appendLine()
                appendLine("The spotlight is still animating from the previous tour's last target position.")
                appendLine("Fix: reset animatedBounds when tour becomes inactive so the next start snaps.")
            },
        )
    }

    // Fixed: animatedBounds reset to Rect.Zero when tour becomes inactive
    @Test
    fun `spotlight snaps to first target on tour restart after cancellation`() = runComposeUiTest {
        mainClock.autoAdvance = false

        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "left"),
                WaypointStep(targetKey = "right"),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .testTag("screenshot"),
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = 20.dp, y = 50.dp)
                            .size(60.dp)
                            .background(Color.White)
                            .waypointTarget(state, "left"),
                    )
                    Box(
                        modifier = Modifier
                            .offset(x = 300.dp, y = 50.dp)
                            .size(60.dp)
                            .background(Color.White)
                            .waypointTarget(state, "right"),
                    )
                }
            }
        }

        mainClock.advanceTimeBy(100)

        // === First tour: start → advance to step 2 → cancel ===
        runOnIdle { state.start() }
        mainClock.advanceTimeBy(500)

        runOnIdle { state.next() }
        mainClock.advanceTimeBy(500) // spotlight at "right"

        runOnIdle { state.stop() } // cancel mid-tour
        mainClock.advanceTimeBy(100)

        assertTrue(!state.isActive)

        // === Restart ===
        runOnIdle { state.start() }
        mainClock.advanceTimeBy(32) // 2 frames — should snap

        val image = onNodeWithTag("screenshot").captureToImage()
        val pixelMap = image.toPixelMap()

        val sampleX = 50.coerceIn(0, image.width - 1)
        val sampleY = 80.coerceIn(0, image.height - 1)
        val pixelAtLeftTarget = pixelMap[sampleX, sampleY]
        val brightness = (pixelAtLeftTarget.red + pixelAtLeftTarget.green + pixelAtLeftTarget.blue) / 3f

        assertTrue(
            brightness > 0.7f,
            "Spotlight did NOT snap on restart after cancellation! " +
                "Pixel brightness at left target: ${"%.2f".format(brightness)} (expected > 0.7). " +
                "Spotlight is animating from the cancelled tour's last position.",
        )
    }
}
