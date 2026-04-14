package com.mohamedrejeb.waypoint.material3

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.KeyboardConfig
import com.mohamedrejeb.waypoint.core.OverlayClickBehavior
import com.mohamedrejeb.waypoint.core.WaypointDefaults
import com.mohamedrejeb.waypoint.core.WaypointHost
import com.mohamedrejeb.waypoint.core.WaypointState

/**
 * Convenience wrapper around [WaypointHost] that provides a Material3-styled
 * default tooltip.
 *
 * ```kotlin
 * WaypointMaterial3Host(state = waypointState) {
 *     MyScreenContent()
 * }
 * ```
 */
@Composable
public fun <K> WaypointMaterial3Host(
    state: WaypointState<K>,
    modifier: Modifier = Modifier,
    highlightStyle: HighlightStyle = WaypointDefaults.HighlightStyle,
    overlayClickBehavior: OverlayClickBehavior = WaypointDefaults.OverlayClickBehavior,
    keyboardConfig: KeyboardConfig = WaypointDefaults.KeyboardConfig,
    tooltipSpacing: Dp = WaypointDefaults.TooltipSpacing,
    screenMargin: Dp = WaypointDefaults.ScreenMargin,
    onTourComplete: (() -> Unit)? = null,
    onTourCancel: (() -> Unit)? = null,
    skipText: String = "Skip",
    nextText: String = "Next",
    backText: String = "Back",
    finishText: String = "Finish",
    showProgress: Boolean = true,
    content: @Composable () -> Unit,
) {
    WaypointHost(
        state = state,
        modifier = modifier,
        highlightStyle = highlightStyle,
        overlayClickBehavior = overlayClickBehavior,
        keyboardConfig = keyboardConfig,
        tooltipSpacing = tooltipSpacing,
        screenMargin = screenMargin,
        onTourComplete = onTourComplete,
        onTourCancel = onTourCancel,
        tooltipContent = { stepScope, resolvedPlacement ->
            val currentStep = state.currentStep
            WaypointMaterial3Tooltip(
                stepScope = stepScope,
                resolvedPlacement = resolvedPlacement,
                title = currentStep?.title,
                description = currentStep?.description,
                skipText = skipText,
                nextText = nextText,
                backText = backText,
                finishText = finishText,
                showProgress = showProgress,
            )
        },
        content = content,
    )
}
