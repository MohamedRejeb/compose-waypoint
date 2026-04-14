package com.mohamedrejeb.waypoint.core

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Host composable that wraps your screen content and renders the tour overlay.
 *
 * Place this at the root of your screen, wrapping all content that contains
 * tour targets:
 *
 * ```kotlin
 * WaypointHost(state = waypointState) {
 *     MyScreenContent()
 * }
 * ```
 *
 * @param state the [WaypointState] managing the tour
 * @param overlayColor the color of the semi-transparent overlay
 * @param overlayAlpha the alpha of the overlay (0f - 1f)
 * @param overlayClickBehavior what happens when the overlay is clicked
 * @param tooltipSpacing spacing between tooltip and target
 * @param screenMargin minimum margin from screen edges for the tooltip
 * @param onTourComplete callback when the tour finishes all steps
 * @param onTourCancel callback when the tour is cancelled/skipped
 * @param tooltipContent composable to render the tooltip; receives [StepScope] and [ResolvedPlacement]
 * @param content the screen content that contains tour targets
 */
@Composable
public fun <K> WaypointHost(
    state: WaypointState<K>,
    modifier: Modifier = Modifier,
    overlayColor: Color = WaypointDefaults.OverlayColor,
    overlayAlpha: Float = WaypointDefaults.OverlayAlpha,
    overlayClickBehavior: OverlayClickBehavior = WaypointDefaults.OverlayClickBehavior,
    tooltipSpacing: Dp = WaypointDefaults.TooltipSpacing,
    screenMargin: Dp = WaypointDefaults.ScreenMargin,
    onTourComplete: (() -> Unit)? = null,
    onTourCancel: (() -> Unit)? = null,
    tooltipContent: @Composable (StepScope, ResolvedPlacement) -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val tooltipSpacingPx = with(density) { tooltipSpacing.toPx() }
    val screenMarginPx = with(density) { screenMargin.toPx() }

    // Track previous isActive state to detect completion/cancellation
    var wasActive by remember { mutableStateOf(false) }
    var previousStepCount by remember { mutableStateOf(0) }

    LaunchedEffect(state.isActive) {
        if (wasActive && !state.isActive) {
            // Tour ended -- determine if it was completion or cancellation
            // If we were on the last step and next() was called, it's completion
            // Otherwise it's cancellation (stop() was called)
        }
        wasActive = state.isActive
    }

    // Animated spotlight bounds
    val animatedBounds = remember { Animatable(Rect.Zero, Rect.VectorConverter) }

    val targetBounds = state.currentTargetBounds

    LaunchedEffect(targetBounds) {
        if (targetBounds != null) {
            if (animatedBounds.value == Rect.Zero) {
                // First step - snap to position
                animatedBounds.snapTo(targetBounds)
            } else {
                // Subsequent steps - animate
                animatedBounds.animateTo(
                    targetValue = targetBounds,
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing,
                    ),
                )
            }
        }
    }

    val shouldShowOverlay = state.isActive && !state.isPaused && targetBounds != null
    val currentStep = state.currentStep

    Box(modifier = modifier) {
        // 1. Screen content
        content()

        // 2. Spotlight overlay
        if (shouldShowOverlay && currentStep != null) {
            val step = currentStep
            SpotlightOverlay(
                targetBounds = animatedBounds.value,
                spotlightShape = step.spotlightShape,
                spotlightPadding = step.spotlightPadding,
                overlayColor = overlayColor,
                overlayAlpha = overlayAlpha,
                onOverlayClick = {
                    when (overlayClickBehavior) {
                        is OverlayClickBehavior.Nothing -> {}
                        is OverlayClickBehavior.Dismiss -> {
                            state.stop()
                            onTourCancel?.invoke()
                        }
                        is OverlayClickBehavior.NextStep -> state.next()
                        is OverlayClickBehavior.Custom -> overlayClickBehavior.action()
                    }
                },
                onTargetClick = {
                    when (step.interaction) {
                        TargetInteraction.None -> {}
                        TargetInteraction.AllowClick -> {}
                        TargetInteraction.ClickToAdvance -> state.next()
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )

            // 3. Tooltip
            val stepScope = StepScopeImpl(
                currentStepIndex = state.currentStepIndex,
                totalSteps = state.steps.size,
                isFirstStep = state.currentStepIndex == 0,
                isLastStep = state.currentStepIndex == state.steps.lastIndex,
                onNext = {
                    val wasLastStep = state.currentStepIndex == state.steps.lastIndex
                    state.next()
                    if (wasLastStep && !state.isActive) {
                        onTourComplete?.invoke()
                    }
                },
                onPrevious = { state.previous() },
                onSkip = {
                    state.stop()
                    onTourCancel?.invoke()
                },
                onClose = {
                    state.stop()
                    onTourCancel?.invoke()
                },
            )

            TooltipPopup(
                visible = true,
                targetBounds = animatedBounds.value,
                placement = step.placement,
                tooltipSpacing = tooltipSpacingPx,
                screenMargin = screenMarginPx,
            ) { resolvedPlacement ->
                // Use step-level custom content or fall back to the host's tooltipContent
                val customContent = step.content
                if (customContent != null) {
                    customContent(stepScope)
                } else {
                    tooltipContent(stepScope, resolvedPlacement)
                }
            }
        }
    }
}
