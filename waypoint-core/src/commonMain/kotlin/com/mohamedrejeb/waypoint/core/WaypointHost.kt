package com.mohamedrejeb.waypoint.core

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.isActive

/**
 * Host composable that wraps your screen content and renders the tour highlight + tooltip.
 *
 * Place this at the root of your screen, wrapping all content that contains tour targets.
 *
 * For tours that span a Dialog, Sheet, or Popup, add a [WaypointOverlayHost] inside that
 * composition tree with the same [WaypointState] so targets register against the correct
 * host.
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
    val hostId = remember { Any() }
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

    // Re-request focus when the tour starts (in case focus was lost while inactive).
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

    WaypointHostScope(
        state = state,
        hostId = hostId,
        isPrimary = true,
        modifier = modifier.then(keyboardModifier),
        highlightStyle = highlightStyle,
        overlayClickBehavior = overlayClickBehavior,
        tooltipSpacing = tooltipSpacing,
        screenMargin = screenMargin,
        onTourComplete = onTourComplete,
        onTourCancel = onTourCancel,
        tooltipContent = tooltipContent,
        content = content,
    )
}

/**
 * Shared rendering logic for [WaypointHost] and [WaypointOverlayHost].
 *
 * Each host gets its own unique [hostId] and registers its [androidx.compose.ui.layout.LayoutCoordinates]
 * into [WaypointState.hostCoordinatesMap]. Overlay + tooltip render only when the
 * current step's target belongs to this host, so multiple hosts can coexist and
 * hand off between screens/modals without each other's targets leaking through.
 *
 * Only the primary host drives tour lifecycle effects (beforeShow, step-transition
 * reset of animatedBounds). Overlay hosts share the same [WaypointState] but stay
 * silent on lifecycle so they don't duplicate side effects.
 */
@Composable
internal fun <K> WaypointHostScope(
    state: WaypointState<K>,
    hostId: Any,
    isPrimary: Boolean,
    modifier: Modifier,
    highlightStyle: HighlightStyle,
    overlayClickBehavior: OverlayClickBehavior,
    tooltipSpacing: Dp,
    screenMargin: Dp,
    onTourComplete: (() -> Unit)?,
    onTourCancel: (() -> Unit)?,
    tooltipContent: @Composable (StepScope, ResolvedPlacement) -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val tooltipSpacingPx = with(density) { tooltipSpacing.toPx() }
    val screenMarginPx = with(density) { screenMargin.toPx() }

    // Animated highlight bounds in this host's local space.
    val animatedBounds = remember { Animatable(Rect.Zero, Rect.VectorConverter) }

    val isOwnedByThisHost = state.currentTargetHostId == hostId

    // Primary-only: step transitions reset animatedBounds if the new target
    // isn't registered yet (e.g. beforeShow will open a modal that mounts the
    // target). Resetting to Rect.Zero keeps stale bounds from the previous step
    // from showing briefly when isStepReady flips back to true before the new
    // target is laid out. This MUST run before the beforeShow gate so
    // animatedBounds is Zero by the time isStepReady becomes true.
    if (isPrimary) {
        LaunchedEffect(state.currentStepIndex) {
            val step = state.currentStep ?: return@LaunchedEffect
            val targetRegistered = state.targetCoordinates[step.targetKey] != null
            if (!targetRegistered && state.isActive) {
                animatedBounds.snapTo(Rect.Zero)
            }

            // Run beforeShow async gate, then mark the step ready.
            val gate = step.beforeShow
            if (gate != null) {
                try {
                    gate()
                } finally {
                    // Skip on cancellation (rapid navigation): the next step's
                    // transitionTo has already set isStepReady for its own target,
                    // and overriding it here would cause flicker.
                    if (isActive) {
                        state.setStepReady(true)
                    }
                }
            } else {
                state.setStepReady(true)
            }
        }

        // Reset animated bounds when the tour becomes inactive so the next
        // start() snaps to the first target instead of animating from the
        // previous tour's last position.
        LaunchedEffect(state.isActive) {
            if (!state.isActive) {
                animatedBounds.snapTo(Rect.Zero)
            }
        }
    } else {
        // Overlay hosts: reset their own animatedBounds when they lose or have
        // not yet gained ownership. That way when ownership swings to this
        // host, animatedBounds is Zero and the new target snaps rather than
        // animating from a stale rect.
        LaunchedEffect(isOwnedByThisHost, state.isActive) {
            if (!isOwnedByThisHost || !state.isActive) {
                animatedBounds.snapTo(Rect.Zero)
            }
        }
    }

    // Only animate to target bounds when the current step belongs to THIS host.
    val targetBounds = if (isOwnedByThisHost) state.currentTargetBounds else null

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

    // Unregister this host when it leaves composition. Any targets still
    // associated with it (hosts disposed before their children) get cleaned up.
    DisposableEffect(hostId) {
        onDispose {
            state.unregisterHost(hostId)
        }
    }

    // Safety net: the modifier already unregisters degenerate bounds, but
    // double-check here in case of unusual states.
    val isTargetVisible = targetBounds != null &&
        targetBounds.width > 1f && targetBounds.height > 1f

    val shouldShowHighlight = state.isActive && !state.isPaused &&
        isTargetVisible && state.isStepReady && isOwnedByThisHost
    // Tooltip visibility: shown while the tour is active and has valid bounds
    // to render at. `animatedBounds != Rect.Zero` means the current step's
    // target has been observed at least once, which gates out the transition
    // window where isStepReady is true but the new target hasn't been laid
    // out yet.
    val shouldShowTooltip = state.isActive && !state.isPaused && state.isStepReady &&
        animatedBounds.value != Rect.Zero && isOwnedByThisHost

    val currentStep = state.currentStep

    CompositionLocalProvider(LocalWaypointHostId provides hostId) {
        Box(
            modifier = modifier
                .onGloballyPositioned { coords ->
                    state.registerHost(hostId, coords)
                },
        ) {
            // 1. Screen content
            content()

            if (currentStep != null && isOwnedByThisHost) {
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

                // Additional bounds are in the same host-relative space since
                // they register through the same modifier against the same host.
                val additionalBounds = step.additionalTargets.mapNotNull { key ->
                    state.targetCoordinates[key]
                }

                // 2. Highlight layer (host-relative bounds, matchParentSize inside host Box)
                if (shouldShowHighlight) {
                    when (resolvedStyle) {
                        is HighlightStyle.Spotlight -> {
                            SpotlightOverlay(
                                targetBounds = animatedBounds.value,
                                additionalBounds = additionalBounds,
                                style = resolvedStyle,
                                allowTargetInteraction = step.interaction == TargetInteraction.AllowClick,
                                onOverlayClick = overlayClickHandler,
                                onTargetClick = targetClickHandler,
                                modifier = Modifier.matchParentSize(),
                            )
                        }

                        is HighlightStyle.Pulse -> {
                            PulseHighlight(
                                targetBounds = animatedBounds.value,
                                additionalBounds = additionalBounds,
                                style = resolvedStyle,
                                modifier = Modifier.matchParentSize(),
                            )
                        }

                        is HighlightStyle.Border -> {
                            BorderHighlight(
                                targetBounds = animatedBounds.value,
                                additionalBounds = additionalBounds,
                                style = resolvedStyle,
                                modifier = Modifier.matchParentSize(),
                            )
                        }

                        is HighlightStyle.Ripple -> {
                            RippleHighlight(
                                targetBounds = animatedBounds.value,
                                additionalBounds = additionalBounds,
                                style = resolvedStyle,
                                modifier = Modifier.matchParentSize(),
                            )
                        }

                        is HighlightStyle.None -> {}

                        is HighlightStyle.Custom -> {
                            resolvedStyle.content(
                                targetBounds,
                                animatedBounds.value,
                            )
                        }
                    }
                }

                // 3. Tooltip + navigation.
                if (shouldShowTooltip) {
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

                    // TooltipPopup positions via WaypointPositionProvider, which
                    // expects window-space bounds. Translate host-relative bounds
                    // by the host's window offset.
                    val rawBounds = targetBounds ?: animatedBounds.value
                    val hostCoords = state.hostCoordinatesMap[hostId]
                    val tooltipTargetBounds = if (hostCoords != null && hostCoords.isAttached) {
                        rawBounds.translate(hostCoords.positionInWindow())
                    } else {
                        rawBounds
                    }

                    TooltipPopup(
                        visible = true,
                        targetBounds = tooltipTargetBounds,
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

                // 4. Event-driven progression trigger
                val trigger = step.advanceOn
                if (trigger is WaypointTrigger.Custom) {
                    LaunchedEffect(state.currentStepIndex) {
                        trigger.await()
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
}

/**
 * Resolves the effective highlight style for a step.
 * If the step uses the default, fall back to the host-level style.
 */
private fun resolveHighlightStyle(
    stepStyle: HighlightStyle,
    hostStyle: HighlightStyle,
): HighlightStyle {
    return if (stepStyle == HighlightStyle.Default) hostStyle else stepStyle
}
