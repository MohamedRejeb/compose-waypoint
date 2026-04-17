package com.mohamedrejeb.waypoint.material3

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.waypoint.core.HighlightStyle
import com.mohamedrejeb.waypoint.core.OverlayClickBehavior
import com.mohamedrejeb.waypoint.core.WaypointDefaults
import com.mohamedrejeb.waypoint.core.WaypointOverlayHost
import com.mohamedrejeb.waypoint.core.WaypointState

/**
 * Convenience wrapper around [WaypointOverlayHost] that renders the Material3-styled
 * default tooltip.
 *
 * Place this inside a Dialog, Sheet, or Popup to let a tour managed by an outer
 * [WaypointMaterial3Host] reach into that modal.
 *
 * ```kotlin
 * WaypointMaterial3Host(state = state) {
 *     MyScreen()
 *
 *     if (showDialog) {
 *         Dialog(onDismissRequest = { showDialog = false }) {
 *             WaypointMaterial3OverlayHost(state = state) {
 *                 DialogContent()
 *             }
 *         }
 *     }
 * }
 * ```
 */
@Composable
public fun <K> WaypointMaterial3OverlayHost(
    state: WaypointState<K>,
    modifier: Modifier = Modifier,
    highlightStyle: HighlightStyle = WaypointDefaults.HighlightStyle,
    overlayClickBehavior: OverlayClickBehavior = WaypointDefaults.OverlayClickBehavior,
    tooltipSpacing: Dp = WaypointDefaults.TooltipSpacing,
    screenMargin: Dp = WaypointDefaults.ScreenMargin,
    skipText: String = "Skip",
    nextText: String = "Next",
    backText: String = "Back",
    finishText: String = "Finish",
    showProgress: Boolean = true,
    content: @Composable () -> Unit,
) {
    WaypointOverlayHost(
        state = state,
        modifier = modifier,
        highlightStyle = highlightStyle,
        overlayClickBehavior = overlayClickBehavior,
        tooltipSpacing = tooltipSpacing,
        screenMargin = screenMargin,
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
