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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
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
    // While a target is not registered anywhere, the primary host stands in.
    val isResponsibleHost = isOwnedByThisHost || (isPrimary && step != null && state.currentTargetHostId == null)

    if (isPrimary) {
        TourEndEffect(state, onTourComplete, onTourCancel)
        StepLifecycleEffect(state, animatedBounds)
        AutoScrollEffect(state)

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

    // The highlight glides only on a step change: the first bounds of a new
    // step generation animate from the previous target, later bounds of the
    // same step (the target moved with a scroll or the keyboard) snap, so the
    // hole never lags behind the target.
    val animatedGeneration = remember { AnimatedGeneration() }
    LaunchedEffect(targetBounds) {
        if (targetBounds == null) return@LaunchedEffect
        val generation = state.stepGeneration
        val isNewStep = generation != animatedGeneration.value
        animatedGeneration.value = generation
        if (animatedBounds.value == Rect.Zero || !isNewStep) {
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

    // Unregister this host when it leaves composition. Any targets still
    // associated with it (hosts disposed before their children) get cleaned up.
    DisposableEffect(hostId) {
        onDispose {
            state.unregisterHost(hostId)
        }
    }

    // Handed down as a function and only invoked while drawing and laying out,
    // so the frames of the bounds animation do not recompose the host.
    val readAnimatedBounds = remember(animatedBounds) { { animatedBounds.value } }

    val isRunning = state.isActive && !state.isPaused
    val isShowingStep = isRunning && !state.isStepHeld && isOwnedByThisHost
    // Pending: held by its gate, or its target never laid out during this
    // visit. A target that scrolls away after the step was shown is not
    // pending, so a pass-through user is never trapped under a cover.
    val isPending = isRunning && step != null &&
        (state.isStepHeld || (step.targetKey != null && targetBounds == null && !state.hasShownCurrentStep))
    val resolvedStyle = step?.highlightStyle ?: highlightStyle
    val coversPending = isPending && isResponsibleHost &&
        resolvedStyle is HighlightStyle.Spotlight && resolvedStyle.coverWhilePending

    CompositionLocalProvider(LocalWaypointHostId provides hostId) {
        Box(
            modifier = modifier
                .onGloballyPositioned { coords ->
                    state.registerHost(hostId, coords)
                },
        ) {
            // 1. Screen content
            content()

            val onOverlayClick: () -> Unit = {
                when (overlayClickBehavior) {
                    is OverlayClickBehavior.Nothing -> {}
                    // onTourCancel fires via the end-event observer.
                    is OverlayClickBehavior.Dismiss -> state.stop()
                    is OverlayClickBehavior.NextStep -> state.next()
                    is OverlayClickBehavior.Custom -> overlayClickBehavior.action()
                }
            }

            // 2. Cover while the step is pending (opt-in): scrim with no
            // cutout, everything blocked, overlay clicks still handled.
            if (coversPending && resolvedStyle is HighlightStyle.Spotlight) {
                SpotlightOverlay(
                    targetBounds = { emptyList() },
                    style = resolvedStyle,
                    passThrough = false,
                    onOverlayClick = onOverlayClick,
                    onTargetClick = {},
                    modifier = Modifier.matchParentSize(),
                )
            }

            // 3. Highlight + tooltip of the current step
            if (step != null && isShowingStep) {
                StepLayers(
                    state = state,
                    step = step,
                    hostId = hostId,
                    targetBounds = targetBounds,
                    animatedBounds = readAnimatedBounds,
                    highlightStyle = resolvedStyle,
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

/** Last step generation the highlight animated or snapped to. Plain holder, not snapshot state. */
private class AnimatedGeneration {
    var value: Int = -1
}

/**
 * Drives what happens when a step becomes current: runs its beforeShow gate,
 * then awaits its advanceOn condition once the step is on screen.
 *
 * Keyed on the step generation, so every entry into a step (including a
 * `stop()` and `start()` within one frame) relaunches it. The generation is
 * captured at launch, and the gate result or the trigger only act while it
 * still matches, which keeps work from an earlier visit from touching the
 * current one.
 */
@Composable
private fun <K> StepLifecycleEffect(
    state: WaypointState<K>,
    animatedBounds: AnimatedBounds,
) {
    LaunchedEffect(state, state.stepGeneration) {
        val step = state.currentStep ?: return@LaunchedEffect
        val generation = state.stepGeneration

        // Drop the previous bounds when the new step has none yet (its target
        // is not registered, for example beforeShow will open the modal that
        // mounts it, or it has no target) and when a tour starts, so the
        // first target snaps into place instead of gliding from stale bounds.
        if (state.isActive && (state.currentTargetBounds == null || state.isFirstStepOfRun)) {
            animatedBounds.snapTo(Rect.Zero)
        }

        step.beforeShow?.let { gate -> state.runGate(gate, generation) }
        if (state.stepGeneration != generation) return@LaunchedEffect

        val advanceOn = step.advanceOn ?: return@LaunchedEffect
        if (!state.isTriggerArmed) return@LaunchedEffect
        // Wait until the step is actually shown so a condition that is already
        // satisfied can't advance past a step the user never saw.
        snapshotFlow { state.isStepVisible }.first { it }
        advanceOn()
        snapshotFlow { !state.isPaused }.first { it }
        if (state.stepGeneration == generation) state.next()
    }
}


/**
 * Brings the current step's target into view once the step is showable: its
 * gate has completed and the target is in the composition. That also covers a
 * target mounted by beforeShow, or one that registers some time after the step
 * was entered.
 *
 * It scrolls once per step. After that the position is the user's: scrolling
 * inside a pass-through step, or the target's bounds changing, never triggers
 * another scroll.
 */
@Composable
private fun <K> AutoScrollEffect(state: WaypointState<K>) {
    LaunchedEffect(state, state.stepGeneration) {
        if (state.currentStep?.targetKey == null) return@LaunchedEffect
        snapshotFlow { state.isActive && state.isStepReady && !state.isPaused }.first { it }

        val registered = state.currentBringIntoViewRequester
        val requester = registered
            ?: snapshotFlow { state.currentBringIntoViewRequester }.filterNotNull().first()
        // A target that only just entered the composition has not been laid
        // out yet, give it a frame so there is a position to scroll to.
        if (registered == null) withFrameNanos { }
        requester.bringIntoView()
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
 * Nothing is marked once the step [generation] has moved on (rapid
 * navigation, or a gate finishing in the same frame as `next()`): the new
 * step's transition has already set the flags for itself.
 */
private suspend fun WaypointState<*>.runGate(gate: suspend () -> Unit, generation: Int) {
    coroutineScope {
        val job = launch(start = CoroutineStart.UNDISPATCHED) { gate() }
        if (!job.isCompleted && stepGeneration == generation) holdStep()
    }
    if (stepGeneration == generation) markStepReady()
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
    animatedBounds: () -> Rect,
    highlightStyle: HighlightStyle,
    onOverlayClick: () -> Unit,
    tooltipSpacing: Dp,
    screenMargin: Dp,
    tooltipContent: @Composable (StepScope) -> Unit,
) {
    // Something of this step is on screen from here on.
    SideEffect { state.noteStepShown() }

    if (step.targetKey == null) {
        // A spotlight still dims and blocks the screen, with nothing cut out.
        // Every other style has nothing to draw without a target.
        if (highlightStyle is HighlightStyle.Spotlight) {
            SpotlightOverlay(
                targetBounds = { emptyList() },
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

    // The modifier already unregisters degenerate bounds, this is a safety net.
    if (targetBounds != null && targetBounds.width > 1f && targetBounds.height > 1f) {
        // Only include additional targets registered against THIS host:
        // bounds are host-relative, so a target living in another host
        // (e.g. inside a Dialog) would draw at a meaningless position.
        val additionalBounds = step.additionalTargets.mapNotNull { key ->
            if (state.targetHostIds[key] == hostId) state.targetCoordinates[key] else null
        }
        TargetHighlight(
            style = highlightStyle,
            targetBounds = targetBounds,
            // Until the bounds animation has caught up with a freshly shown
            // target (animated bounds still Zero), draw at the target itself.
            highlightBounds = { animatedBounds().takeUnless { it == Rect.Zero } ?: targetBounds },
            additionalBounds = additionalBounds,
            interaction = step.interaction,
            onOverlayClick = onOverlayClick,
            onTargetClick = { if (step.interaction == TargetInteraction.ClickToAdvance) state.next() },
        )
    }

    // The tooltip stays up while the tour has bounds to render at, even when
    // the target is momentarily unregistered (scrolled out of view).
    val anchorBounds = targetBounds ?: animatedBounds()
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
    advancesAutomatically = isTriggerArmed,
)

/**
 * The highlight layer for a step with a target, sized to the host.
 *
 * [highlightBounds] is the animated position of the primary target. The
 * built-in styles read it while drawing or laying out; only a custom style,
 * whose content takes the bounds as a parameter, recomposes with it.
 */
@Composable
private fun BoxScope.TargetHighlight(
    style: HighlightStyle,
    targetBounds: Rect,
    highlightBounds: () -> Rect,
    additionalBounds: List<Rect>,
    interaction: TargetInteraction,
    onOverlayClick: () -> Unit,
    onTargetClick: () -> Unit,
) {
    val allBounds: () -> List<Rect> = { listOf(highlightBounds()) + additionalBounds }
    when (style) {
        is HighlightStyle.Spotlight -> SpotlightOverlay(
            targetBounds = allBounds,
            style = style,
            passThrough = interaction == TargetInteraction.PassThrough,
            onOverlayClick = onOverlayClick,
            onTargetClick = onTargetClick,
            modifier = Modifier.matchParentSize(),
        )

        is HighlightStyle.Pulse -> PulseHighlight(
            targetBounds = allBounds,
            style = style,
            modifier = Modifier.matchParentSize(),
        )

        is HighlightStyle.Border -> BorderHighlight(
            targetBounds = allBounds,
            style = style,
            modifier = Modifier.matchParentSize(),
        )

        is HighlightStyle.Ripple -> RippleHighlight(
            targetBounds = allBounds,
            style = style,
            modifier = Modifier.matchParentSize(),
        )

        is HighlightStyle.None -> {}

        is HighlightStyle.Custom -> style.content(targetBounds, highlightBounds(), additionalBounds)
    }
}
