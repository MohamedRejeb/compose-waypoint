package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests that the tooltip position remains stable during spotlight
 * transitions between steps.
 *
 * The spotlight animates (400ms tween) from one target to the next,
 * but the tooltip should NOT drift along with the interpolating bounds.
 * It should either:
 * - Snap immediately to the new step's target position, or
 * - Stay at the old position and then jump to the new one
 *
 * What it must NOT do: slide/drift through intermediate positions
 * frame-by-frame during the spotlight animation.
 */
@OptIn(ExperimentalTestApi::class)
class TooltipStabilityTest {

    /**
     * Maximum allowed tooltip drift (in dp) during a spotlight transition.
     * Any movement beyond this threshold between consecutive frames
     * indicates the tooltip is tracking the animated spotlight bounds.
     *
     * A small tolerance is needed for subpixel rounding / layout jitter.
     */
    private val maxDriftToleranceDp = 2f

    // Fixed: tooltip now uses raw targetBounds instead of animatedBounds
    @Test
    fun `tooltip does not drift during spotlight transition between steps`() = runComposeUiTest {
        mainClock.autoAdvance = false

        val state = WaypointState(
            steps = listOf(
                WaypointStep(
                    targetKey = "left",
                    placement = TooltipPlacement.Bottom,
                ),
                WaypointStep(
                    targetKey = "right",
                    placement = TooltipPlacement.Bottom,
                ),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Two targets placed far apart horizontally
                    Box(
                        modifier = Modifier
                            .offset(x = 20.dp, y = 50.dp)
                            .size(60.dp)
                            .waypointTarget(state, "left"),
                    )
                    Box(
                        modifier = Modifier
                            .offset(x = 300.dp, y = 50.dp)
                            .size(60.dp)
                            .waypointTarget(state, "right"),
                    )
                }
            }
        }

        // Let initial composition settle
        mainClock.advanceTimeBy(16)

        // Start tour on step 1 ("left")
        runOnIdle { state.start() }
        // Settle: let the tooltip appear and initial bounds snap
        mainClock.advanceTimeBy(500)

        // Record the tooltip position at step 1
        val step1Bounds = onNodeWithTag("tooltip").getBoundsInRoot()

        // Advance to step 2 ("right") — triggers 400ms spotlight animation
        runOnIdle { state.next() }
        mainClock.advanceTimeBy(16) // one frame to start the animation

        // Sample tooltip position at multiple frames during the 400ms animation
        val samples = mutableListOf<DpRect>()
        val frameInterval = 32L // ~30fps sampling
        val animationDuration = 400L
        var elapsed = 16L // already advanced one frame

        while (elapsed < animationDuration) {
            mainClock.advanceTimeBy(frameInterval)
            elapsed += frameInterval

            try {
                val bounds = onNodeWithTag("tooltip").getBoundsInRoot()
                samples.add(bounds)
            } catch (_: AssertionError) {
                // Tooltip may briefly not exist during transition — that's OK
            }
        }

        // Advance past the animation to let everything settle
        mainClock.advanceTimeBy(200)

        val step2Bounds = try {
            onNodeWithTag("tooltip").getBoundsInRoot()
        } catch (_: AssertionError) {
            fail("Tooltip not visible after animation settled")
        }

        // Now analyze: during the animation, the tooltip should NOT have drifted
        // through intermediate positions. Check that each mid-animation sample
        // is close to either step1Bounds OR step2Bounds — not somewhere in between.
        val driftingFrames = mutableListOf<String>()

        for ((i, sample) in samples.withIndex()) {
            val distToStep1 = maxOf(
                abs(sample.left.value - step1Bounds.left.value),
                abs(sample.top.value - step1Bounds.top.value),
            )
            val distToStep2 = maxOf(
                abs(sample.left.value - step2Bounds.left.value),
                abs(sample.top.value - step2Bounds.top.value),
            )

            val isNearStep1 = distToStep1 <= maxDriftToleranceDp
            val isNearStep2 = distToStep2 <= maxDriftToleranceDp

            if (!isNearStep1 && !isNearStep2) {
                driftingFrames.add(
                    "Frame $i: tooltip at (${sample.left}, ${sample.top}) — " +
                        "dist to step1=${distToStep1.format()}dp, " +
                        "dist to step2=${distToStep2.format()}dp",
                )
            }
        }

        assertTrue(
            driftingFrames.isEmpty(),
            buildString {
                appendLine("Tooltip drifted during spotlight animation!")
                appendLine("Step 1 tooltip: (${step1Bounds.left}, ${step1Bounds.top})")
                appendLine("Step 2 tooltip: (${step2Bounds.left}, ${step2Bounds.top})")
                appendLine("${driftingFrames.size} frames had intermediate positions:")
                driftingFrames.forEach { appendLine("  $it") }
                appendLine()
                appendLine("The tooltip should snap to its new position, not drift with the spotlight animation.")
            },
        )
    }

    // Fixed: tooltip now uses raw targetBounds instead of animatedBounds
    @Test
    fun `tooltip does not drift during vertical transition`() = runComposeUiTest {
        mainClock.autoAdvance = false

        val state = WaypointState(
            steps = listOf(
                WaypointStep(targetKey = "top", placement = TooltipPlacement.Bottom),
                WaypointStep(targetKey = "bottom", placement = TooltipPlacement.Top),
            ),
        )

        setContent {
            WaypointHost(
                state = state,
                tooltipContent = { _, _ ->
                    BasicText("Tooltip", Modifier.testTag("tooltip"))
                },
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .offset(x = 100.dp, y = 30.dp)
                            .size(60.dp)
                            .waypointTarget(state, "top"),
                    )
                    Box(
                        modifier = Modifier
                            .offset(x = 100.dp, y = 400.dp)
                            .size(60.dp)
                            .waypointTarget(state, "bottom"),
                    )
                }
            }
        }

        mainClock.advanceTimeBy(16)
        runOnIdle { state.start() }
        mainClock.advanceTimeBy(500)

        val step1Bounds = onNodeWithTag("tooltip").getBoundsInRoot()

        runOnIdle { state.next() }
        mainClock.advanceTimeBy(16)

        val samples = mutableListOf<DpRect>()
        var elapsed = 16L
        while (elapsed < 400L) {
            mainClock.advanceTimeBy(32)
            elapsed += 32
            try {
                samples.add(onNodeWithTag("tooltip").getBoundsInRoot())
            } catch (_: AssertionError) { /* tooltip may be absent briefly */ }
        }

        mainClock.advanceTimeBy(200)
        val step2Bounds = try {
            onNodeWithTag("tooltip").getBoundsInRoot()
        } catch (_: AssertionError) {
            fail("Tooltip not visible after animation settled")
        }

        val driftingFrames = samples.filter { sample ->
            val distToStep1 = maxOf(
                abs(sample.left.value - step1Bounds.left.value),
                abs(sample.top.value - step1Bounds.top.value),
            )
            val distToStep2 = maxOf(
                abs(sample.left.value - step2Bounds.left.value),
                abs(sample.top.value - step2Bounds.top.value),
            )
            distToStep1 > maxDriftToleranceDp && distToStep2 > maxDriftToleranceDp
        }

        assertTrue(
            driftingFrames.isEmpty(),
            "Tooltip drifted during vertical transition! " +
                "${driftingFrames.size} of ${samples.size} frames at intermediate positions. " +
                "Step 1 at (${step1Bounds.left}, ${step1Bounds.top}), " +
                "Step 2 at (${step2Bounds.left}, ${step2Bounds.top}).",
        )
    }

    private fun Float.format(): String {
        val int = toInt()
        val frac = ((this - int) * 10).toInt().let { if (it < 0) -it else it }
        return "$int.$frac"
    }
}
