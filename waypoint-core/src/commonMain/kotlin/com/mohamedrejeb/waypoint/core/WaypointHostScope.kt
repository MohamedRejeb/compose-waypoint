package com.mohamedrejeb.waypoint.core

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private typealias AnimatedBounds = Animatable<Rect, AnimationVector4D>

/**
 * Shared rendering logic for [WaypointHost] and [WaypointOverlayHost].
 *
 * Each host gets its own unique [hostId] and registers its [androidx.compose.ui.layout.LayoutCoordinates]
 * into [WaypointState.hostCoordinatesMap]. Overlay + tooltip render only when the
 * current step belongs to this host (its target is registered against this
 * host, or it has no target and this is the primary host), so multiple hosts
 * can coexist and hand off between screens/modals without each other's
 * targets leaking through.
 *
 * Only the primary host drives tour lifecycle effects (beforeShow, advanceOn,
 * end callbacks). Overlay hosts share the same [WaypointState] but stay silent
 * on lifecycle so they don't duplicate side effects.
 */
@OptIn(ExperimentalWaypointApi::class)
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
    tooltipContent: @Composable (StepScope) -> Unit,
    content: @Composable () -> Unit,
) {
    // Animated highlight bounds in this host's local space.
    val animatedBounds = remember { Animatable(Rect.Zero, Rect.VectorConverter) }

    val step = state.currentStep
    val isOwnedByThisHost = when {
        step == null -> false
        step.targetKey == null -> isPrimary
        else -> state.currentTargetHostId == hostId
    }

    if (isPrimary) {
        TourEndEffect(state, onTourComplete, onTourCancel)
        StepLifecycleEffect(state, animatedBounds)

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

    val isShowingStep = state.isActive && !state.isPaused && !state.isStepHeld && isOwnedByThisHost

    CompositionLocalProvider(LocalWaypointHostId provides hostId) {
        Box(
            modifier = modifier
                .onGloballyPositioned { coords ->
                    state.registerHost(hostId, coords)
                },
        ) {
            // 1. Screen content
            content()

            // 2. Highlight + tooltip of the current step
            if (step != null && isShowingStep) {
                val onOverlayClick: () -> Unit = {
                    when (overlayClickBehavior) {
                        is OverlayClickBehavior.Nothing -> {}
                        // onTourCancel fires via the end-event observer.
                        is OverlayClickBehavior.Dismiss -> state.stop()
                        is OverlayClickBehavior.NextStep -> state.next()
                        is OverlayClickBehavior.Custom -> overlayClickBehavior.action()
                    }
                }
                StepLayers(
                    state = state,
                    step = step,
                    hostId = hostId,
                    targetBounds = targetBounds,
                    animatedBounds = animatedBounds.value,
                    highlightStyle = step.highlightStyle ?: highlightStyle,
                    onOverlayClick = onOverlayClick,
                    tooltipSpacing = tooltipSpacing,
                    screenMargin = screenMargin,
                    tooltipContent = step.content ?: tooltipContent,
                )
            }
        }
    }
}

/**
 * Fires onTourComplete/onTourCancel from tour end events recorded on the
 * state. This covers every way a tour can end: tooltip buttons in this host or
 * in any overlay host (Dialog/Sheet), keyboard shortcuts, overlay clicks,
 * advanceOn, and direct state.stop() calls.
 */
@Composable
private fun TourEndEffect(
    state: WaypointState<*>,
    onTourComplete: (() -> Unit)?,
    onTourCancel: (() -> Unit)?,
) {
    val currentOnTourComplete by rememberUpdatedState(onTourComplete)
    val currentOnTourCancel by rememberUpdatedState(onTourCancel)
    LaunchedEffect(state) {
        snapshotFlow { state.endEventCount }
            .drop(1)
            .collect {
                when (state.lastEndReason) {
                    WaypointEndReason.Completed -> currentOnTourComplete?.invoke()
                    WaypointEndReason.Cancelled -> currentOnTourCancel?.invoke()
                    null -> Unit
                }
            }
    }
}

/**
 * Drives what happens when a step becomes current: runs its beforeShow gate,
 * then awaits its advanceOn condition once the step is on screen.
 */
@Composable
private fun <K> StepLifecycleEffect(
    state: WaypointState<K>,
    animatedBounds: AnimatedBounds,
) {
    LaunchedEffect(state, state.currentStepIndex) {
        val step = state.currentStep ?: return@LaunchedEffect

        // If the new step has no bounds yet (its target is not registered, for
        // example beforeShow will open the modal that mounts it, or it has no
        // target), drop the previous step's bounds so they cannot show briefly
        // once the step is revealed. The next target then snaps into place.
        if (state.isActive && state.currentTargetBounds == null) {
            animatedBounds.snapTo(Rect.Zero)
        }

        step.beforeShow?.let { gate -> state.runGate(gate) }

        val advanceOn = step.advanceOn ?: return@LaunchedEffect
        // Wait until the step is actually shown so a condition that is already
        // satisfied can't advance past a step the user never saw.
        snapshotFlow { state.isStepVisible }.first { it }
        advanceOn()
        snapshotFlow { !state.isPaused }.first { it }
        state.next()
    }
}

/**
 * Runs a beforeShow [gate] and marks the step ready when it returns.
 *
 * The gate is started undispatched: one that returns without suspending is
 * done before this function touches the step's visibility, so an already
 * visible target is never hidden for a frame. Only a gate that is still
 * running after that first run hides the step until it completes.
 *
 * On cancellation (rapid navigation) nothing is marked: the next step's
 * transition has already set the flags for itself.
 */
private suspend fun WaypointState<*>.runGate(gate: suspend () -> Unit) {
    coroutineScope {
        val job = launch(start = CoroutineStart.UNDISPATCHED) { gate() }
        if (!job.isCompleted) holdStep()
    }
    markStepReady()
}

/**
 * The highlight and tooltip of [step], either anchored to its target or, for
 * a step without a target, centered over the host.
 */
@Composable
private fun <K> BoxScope.StepLayers(
    state: WaypointState<K>,
    step: WaypointStep<K>,
    hostId: Any,
    targetBounds: Rect?,
    animatedBounds: Rect,
    highlightStyle: HighlightStyle,
    onOverlayClick: () -> Unit,
    tooltipSpacing: Dp,
    screenMargin: Dp,
    tooltipContent: @Composable (StepScope) -> Unit,
) {
    if (step.targetKey == null) {
        // A spotlight still dims and blocks the screen, with nothing cut out.
        // Every other style has nothing to draw without a target.
        if (highlightStyle is HighlightStyle.Spotlight) {
            SpotlightOverlay(
                targetBounds = emptyList(),
                style = highlightStyle,
                passThrough = false,
                onOverlayClick = onOverlayClick,
                onTargetClick = {},
                modifier = Modifier.matchParentSize(),
            )
        }
        // Keyed by step so each step gets a fresh popup and enter animation.
        key(state.currentStepIndex) {
            CenteredTooltipPopup {
                tooltipContent(state.stepScope(step, placement = null))
            }
        }
        return
    }

    // Until the bounds animation has caught up with a freshly shown target
    // (animatedBounds is still Zero), draw at the target itself.
    val highlightBounds = if (animatedBounds == Rect.Zero) targetBounds else animatedBounds

    // The modifier already unregisters degenerate bounds, this is a safety net.
    val isTargetVisible = targetBounds != null && targetBounds.width > 1f && targetBounds.height > 1f
    if (isTargetVisible && highlightBounds != null) {
        // Only include additional targets registered against THIS host:
        // bounds are host-relative, so a target living in another host
        // (e.g. inside a Dialog) would draw at a meaningless position.
        val additionalBounds = step.additionalTargets.mapNotNull { key ->
            if (state.targetHostIds[key] == hostId) state.targetCoordinates[key] else null
        }
        TargetHighlight(
            style = highlightStyle,
            targetBounds = targetBounds,
            highlightBounds = highlightBounds,
            additionalBounds = additionalBounds,
            interaction = step.interaction,
            onOverlayClick = onOverlayClick,
            onTargetClick = { if (step.interaction == TargetInteraction.ClickToAdvance) state.next() },
        )
    }

    // The tooltip stays up while the tour has bounds to render at, even when
    // the target is momentarily unregistered (scrolled out of view).
    val anchorBounds = targetBounds ?: animatedBounds
    if (anchorBounds != Rect.Zero) {
        val density = LocalDensity.current
        // Keyed by step so each step gets a fresh popup and enter animation.
        // Its first frame is transparent, which also covers the single frame
        // a step is composed before a suspending beforeShow gate hides it.
        key(state.currentStepIndex) {
            // TooltipPopup expects bounds relative to its anchor, which is this
            // host's Box (the popup is composed inside it), so host-local
            // bounds can be passed straight through. The popup's own
            // anchorBounds supply the window offset.
            TooltipPopup(
                targetBounds = anchorBounds,
                placement = step.placement,
                tooltipSpacing = with(density) { tooltipSpacing.toPx() },
                screenMargin = with(density) { screenMargin.toPx() },
            ) { resolvedPlacement ->
                tooltipContent(state.stepScope(step, resolvedPlacement))
            }
        }
    }
}

/**
 * Builds the scope handed to tooltip content. Step numbers and flags are
 * computed over currently-visible steps (showIf), so progress text and the
 * Finish button stay correct when steps are conditionally hidden.
 */
private fun <K> WaypointState<K>.stepScope(
    step: WaypointStep<K>,
    placement: ResolvedPlacement?,
): StepScope = StepScopeImpl(
    state = this,
    title = step.title,
    description = step.description,
    placement = placement,
    currentStepIndex = currentStepIndex,
    currentStepNumber = visibleStepNumber(currentStepIndex),
    totalSteps = visibleStepCount(),
    isFirstStep = !hasVisibleStepBefore(currentStepIndex),
    isLastStep = !hasVisibleStepAfter(currentStepIndex),
)

/** The highlight layer for a step with a target, sized to the host. */
@Composable
private fun BoxScope.TargetHighlight(
    style: HighlightStyle,
    targetBounds: Rect,
    highlightBounds: Rect,
    additionalBounds: List<Rect>,
    interaction: TargetInteraction,
    onOverlayClick: () -> Unit,
    onTargetClick: () -> Unit,
) {
    when (style) {
        is HighlightStyle.Spotlight -> SpotlightOverlay(
            targetBounds = listOf(highlightBounds) + additionalBounds,
            style = style,
            passThrough = interaction == TargetInteraction.PassThrough,
            onOverlayClick = onOverlayClick,
            onTargetClick = onTargetClick,
            modifier = Modifier.matchParentSize(),
        )

        is HighlightStyle.Pulse -> PulseHighlight(
            targetBounds = highlightBounds,
            additionalBounds = additionalBounds,
            style = style,
            modifier = Modifier.matchParentSize(),
        )

        is HighlightStyle.Border -> BorderHighlight(
            targetBounds = highlightBounds,
            additionalBounds = additionalBounds,
            style = style,
            modifier = Modifier.matchParentSize(),
        )

        is HighlightStyle.Ripple -> RippleHighlight(
            targetBounds = highlightBounds,
            additionalBounds = additionalBounds,
            style = style,
            modifier = Modifier.matchParentSize(),
        )

        is HighlightStyle.None -> {}

        is HighlightStyle.Custom -> style.content(targetBounds, highlightBounds)
    }
}
