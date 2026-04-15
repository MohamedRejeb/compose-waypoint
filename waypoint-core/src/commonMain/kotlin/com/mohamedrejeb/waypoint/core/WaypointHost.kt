package com.mohamedrejeb.waypoint.core

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp

/**
 * Host composable that wraps your screen content and renders the tour highlight + tooltip.
 *
 * Place this at the root of your screen, wrapping all content that contains tour targets.
 *
 * @param state the [WaypointState] managing the tour
 * @param highlightStyle default highlight style for all steps (overridable per-step)
 * @param overlayClickBehavior what happens when the overlay is clicked (only applies to Spotlight)
 * @param keyboardConfig keyboard navigation settings (arrow keys, Escape)
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
    keyboardConfig: KeyboardConfig = WaypointDefaults.KeyboardConfig,
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
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // Clear child focus (e.g. text fields) when transitioning between steps
    // or when the tour stops, so focused inputs don't persist into the next step.
    // Then re-request focus on the host for keyboard navigation.
    LaunchedEffect(state.currentStepIndex) {
        focusManager.clearFocus()
        if (state.isActive && !state.isPaused) {
            if (keyboardConfig.enabled) {
                focusRequester.requestFocus()
            }
            state.scrollCurrentTargetIntoView()
        }
    }

    // Animated highlight bounds
    val animatedBounds = remember { Animatable(Rect.Zero, Rect.VectorConverter) }

    // Reset animated bounds when the tour becomes inactive so the next
    // start() snaps to the first target instead of animating from the
    // previous tour's last position.
    LaunchedEffect(state.isActive) {
        if (!state.isActive) {
            animatedBounds.snapTo(Rect.Zero)
        }
    }

    val targetBounds = state.currentTargetBounds

    LaunchedEffect(targetBounds) {
        if (targetBounds != null) {
            if (animatedBounds.value == Rect.Zero) {
                // First step of a tour (or after reset) — snap immediately
                animatedBounds.snapTo(targetBounds)
            } else {
                // Mid-tour step transition — animate smoothly
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

    // Keyboard navigation: focusable host with onPreviewKeyEvent.
    // onPreviewKeyEvent intercepts during the preview phase, before children.
    // Tour navigation keys (arrows, escape) are consumed; other keys pass through
    // to child composables (text fields, etc.) when AllowClick is active.
    LaunchedEffect(state.isActive) {
        if (state.isActive && keyboardConfig.enabled) {
            focusRequester.requestFocus()
        }
    }

    val keyboardModifier = if (keyboardConfig.enabled) {
        Modifier
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { event ->
                if (!state.isActive || event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                when {
                    event.key in keyboardConfig.nextKeys -> {
                        val wasLastStep = state.currentStepIndex == state.steps.lastIndex
                        state.next()
                        if (wasLastStep && !state.isActive) {
                            onTourComplete?.invoke()
                        }
                        true
                    }
                    event.key in keyboardConfig.previousKeys -> {
                        state.previous()
                        true
                    }
                    event.key in keyboardConfig.dismissKeys -> {
                        state.stop()
                        onTourCancel?.invoke()
                        true
                    }
                    else -> false
                }
            }
            .focusable()
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(keyboardModifier)
            .onGloballyPositioned { coordinates ->
                state.hostCoordinates = coordinates
            },
    ) {
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
                        allowTargetInteraction = step.interaction == TargetInteraction.AllowClick,
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

            // Tooltip uses raw (snapped) bounds, not the animated bounds.
            // The spotlight animates its cutout position smoothly, but the tooltip
            // should snap to the new target immediately to avoid drifting through
            // intermediate positions during the 400ms transition.
            TooltipPopup(
                visible = true,
                targetBounds = targetBounds,
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

            // 4. Event-driven progression trigger
            // When a step uses a Custom trigger, launch a coroutine that
            // awaits the trigger condition and auto-advances when it returns.
            // The coroutine is cancelled when the step changes or tour stops
            // (because this composable leaves composition).
            val trigger = step.advanceOn
            if (trigger is WaypointTrigger.Custom) {
                LaunchedEffect(state.currentStepIndex) {
                    trigger.await()
                    // Trigger completed -- advance to next step
                    val wasLastStep = state.currentStepIndex == state.steps.lastIndex
                    state.next()
                    if (wasLastStep && !state.isActive) {
                        onTourComplete?.invoke()
                    }
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
