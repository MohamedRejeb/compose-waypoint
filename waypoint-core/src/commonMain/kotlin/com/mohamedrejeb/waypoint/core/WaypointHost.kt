package com.mohamedrejeb.waypoint.core

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Host composable that wraps your screen content and renders the tour highlight + tooltip.
 *
 * Place this at the root of your screen, wrapping all content that contains tour targets.
 *
 * @param state the [WaypointState] managing the tour
 * @param highlightStyle default highlight style for all steps (overridable per-step)
 * @param overlayClickBehavior what happens when the overlay is clicked (only applies to Spotlight)
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
    highlightStyle: HighlightStyle = WaypointDefaults.HighlightStyle,
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

    // Auto-scroll the current target into view when the step changes
    LaunchedEffect(state.currentStepIndex) {
        if (state.isActive && !state.isPaused) {
            state.scrollCurrentTargetIntoView()
        }
    }

    // Animated highlight bounds
    val animatedBounds = remember { Animatable(Rect.Zero, Rect.VectorConverter) }

    val targetBounds = state.currentTargetBounds

    LaunchedEffect(targetBounds) {
        if (targetBounds != null) {
            if (animatedBounds.value == Rect.Zero) {
                animatedBounds.snapTo(targetBounds)
            } else {
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

    val shouldShowHighlight = state.isActive && !state.isPaused && targetBounds != null
    val currentStep = state.currentStep

    Box(modifier = modifier) {
        // 1. Screen content
        content()

        // 2. Highlight layer + Tooltip
        if (shouldShowHighlight && currentStep != null) {
            val step = currentStep
            val resolvedStyle = resolveHighlightStyle(step.highlightStyle, highlightStyle)

            val overlayClickHandler: () -> Unit = {
                when (overlayClickBehavior) {
                    is OverlayClickBehavior.Nothing -> {}
                    is OverlayClickBehavior.Dismiss -> {
                        state.stop()
                        onTourCancel?.invoke()
                    }
                    is OverlayClickBehavior.NextStep -> state.next()
                    is OverlayClickBehavior.Custom -> overlayClickBehavior.action()
                }
            }

            val targetClickHandler: () -> Unit = {
                when (step.interaction) {
                    TargetInteraction.None -> {}
                    TargetInteraction.AllowClick -> {}
                    TargetInteraction.ClickToAdvance -> state.next()
                }
            }

            // Dispatch highlight rendering
            when (resolvedStyle) {
                is HighlightStyle.Spotlight -> {
                    SpotlightOverlay(
                        targetBounds = animatedBounds.value,
                        style = resolvedStyle,
                        onOverlayClick = overlayClickHandler,
                        onTargetClick = targetClickHandler,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is HighlightStyle.Pulse -> {
                    PulseHighlight(
                        targetBounds = animatedBounds.value,
                        style = resolvedStyle,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is HighlightStyle.Border -> {
                    BorderHighlight(
                        targetBounds = animatedBounds.value,
                        style = resolvedStyle,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is HighlightStyle.Ripple -> {
                    RippleHighlight(
                        targetBounds = animatedBounds.value,
                        style = resolvedStyle,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is HighlightStyle.None -> {
                    // No highlight layer
                }

                is HighlightStyle.Custom -> {
                    resolvedStyle.content(
                        targetBounds,
                        animatedBounds.value,
                    )
                }
            }

            // 3. Tooltip (always shown regardless of highlight style)
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

/**
 * Resolves the effective highlight style for a step.
 * If the step uses the default, fall back to the host-level style.
 */
private fun resolveHighlightStyle(
    stepStyle: HighlightStyle,
    hostStyle: HighlightStyle,
): HighlightStyle {
    // If the step explicitly set a style (anything other than the default Spotlight()),
    // use it. Otherwise use the host-level default.
    return if (stepStyle == HighlightStyle.Default) hostStyle else stepStyle
}
