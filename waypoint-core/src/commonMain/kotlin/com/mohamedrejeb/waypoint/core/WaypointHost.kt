package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.flow.first

/**
 * Host composable that wraps your screen content and renders the tour highlight + tooltip.
 *
 * Place this at the root of your screen, wrapping all content that contains tour targets.
 *
 * For tours that span a Dialog, Sheet, or Popup, add a [WaypointOverlayHost] inside that
 * composition tree with the same [WaypointState] so targets register against the correct
 * host.
 *
 * Keyboard: while a step is shown the host takes keyboard focus so the
 * [keyboardConfig] keys work, except during a [TargetInteraction.PassThrough]
 * step, where focus is left to the app and only the dismiss keys are handled
 * (the user may be typing in the target). While a beforeShow gate holds a
 * step, only the dismiss keys are handled as well.
 *
 * Compose exactly one `WaypointHost` per [WaypointState] at a time. It drives
 * the tour lifecycle; use [WaypointOverlayHost] for dialogs and sheets.
 *
 * @param state the [WaypointState] managing the tour
 * @param highlightStyle default highlight style for all steps (overridable per-step)
 * @param overlayClickBehavior what happens when the overlay is clicked (only applies to Spotlight)
 * @param keyboardConfig keyboard navigation settings (arrow keys, Escape)
 * @param tooltipSpacing spacing between tooltip and target
 * @param screenMargin minimum margin from screen edges for the tooltip
 * @param onTourComplete callback when the tour finishes all steps
 * @param onTourCancel callback when the tour is cancelled/skipped
 * @param tooltipContent composable to render the tooltip of every step that has
 *   no content of its own; the [StepScope] carries the step's texts, the
 *   resolved placement, progress and navigation
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
    tooltipContent: @Composable (StepScope) -> Unit,
    content: @Composable () -> Unit,
) {
    val hostId = remember { Any() }
    val focusRequester = remember { FocusRequester() }

    // The host takes focus for keyboard navigation once the step is on
    // screen, but never during a pass-through step: there the user works
    // inside the target (for example types in a text field) and focus belongs
    // to the app. While a gate holds the step, focus stays where it is too.
    LaunchedEffect(state, state.stepGeneration) {
        if (!keyboardConfig.enabled || state.currentStep?.isPassThrough != false) return@LaunchedEffect
        snapshotFlow { state.isStepVisible }.first { it }
        focusRequester.requestFocus()
    }

    // Focusable only while a tour runs, so an idle host never takes part in
    // the app's focus traversal.
    val keyboardModifier = if (keyboardConfig.enabled && state.isActive) {
        Modifier
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { event -> state.handleKeyEvent(event, keyboardConfig) }
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
 * Applies a key press to the tour. Returns true when the key was handled.
 *
 * Next and previous keys (arrows, Enter) are handled only while the step is
 * on screen and not a pass-through step; during a pass-through step or while
 * a gate holds the step they reach the app, where the user may be typing. The
 * dismiss keys work whenever the tour is active. Nothing is handled while the
 * tour is inactive or paused.
 */
private fun <K> WaypointState<K>.handleKeyEvent(event: KeyEvent, config: KeyboardConfig): Boolean {
    val step = currentStep
    if (!isActive || isPaused || step == null || event.type != KeyEventType.KeyDown) return false

    val navigationKeysEnabled = isStepVisible && !step.isPassThrough
    return when {
        navigationKeysEnabled && event.key in config.nextKeys -> {
            next()
            true
        }
        navigationKeysEnabled && event.key in config.previousKeys -> {
            previous()
            true
        }
        event.key in config.dismissKeys -> {
            stop()
            true
        }
        else -> false
    }
}
