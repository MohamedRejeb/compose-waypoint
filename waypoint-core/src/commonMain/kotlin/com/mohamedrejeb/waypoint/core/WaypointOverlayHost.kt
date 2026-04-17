package com.mohamedrejeb.waypoint.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Secondary host for tours that span multiple composition trees.
 *
 * Place this inside a [androidx.compose.ui.window.Dialog], a
 * [androidx.compose.material3.ModalBottomSheet], or an [androidx.compose.ui.window.Popup]
 * when you want the tour (managed by an outer [WaypointHost]) to continue targeting
 * elements inside that modal. Both hosts share the same [WaypointState]; targets
 * register against the nearest host, and overlay + tooltip render in the host
 * that owns the current step's target.
 *
 * Unlike [WaypointHost], this host does not own keyboard handling or tour-lifecycle
 * callbacks — those responsibilities stay on the primary host.
 *
 * ```kotlin
 * WaypointHost(state = state) {
 *     MyScreen()
 *
 *     if (showDialog) {
 *         Dialog(onDismissRequest = { showDialog = false }) {
 *             WaypointOverlayHost(state = state) {
 *                 DialogContent() // contains waypointTarget modifiers
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param state the [WaypointState] shared with the primary [WaypointHost]
 * @param highlightStyle default highlight style for steps rendered in this host
 * @param overlayClickBehavior what happens when the overlay is clicked
 * @param tooltipSpacing spacing between tooltip and target
 * @param screenMargin minimum margin from screen edges for the tooltip
 * @param tooltipContent composable to render the tooltip; receives [StepScope] and [ResolvedPlacement]
 * @param content the modal content that contains tour targets
 */
@Composable
public fun <K> WaypointOverlayHost(
    state: WaypointState<K>,
    modifier: Modifier = Modifier,
    highlightStyle: HighlightStyle = WaypointDefaults.HighlightStyle,
    overlayClickBehavior: OverlayClickBehavior = WaypointDefaults.OverlayClickBehavior,
    tooltipSpacing: Dp = WaypointDefaults.TooltipSpacing,
    screenMargin: Dp = WaypointDefaults.ScreenMargin,
    tooltipContent: @Composable (StepScope, ResolvedPlacement) -> Unit,
    content: @Composable () -> Unit,
) {
    val hostId = remember { Any() }

    WaypointHostScope(
        state = state,
        hostId = hostId,
        isPrimary = false,
        modifier = modifier,
        highlightStyle = highlightStyle,
        overlayClickBehavior = overlayClickBehavior,
        tooltipSpacing = tooltipSpacing,
        screenMargin = screenMargin,
        // Tour-lifecycle callbacks belong to the primary host; this host never fires them.
        onTourComplete = null,
        onTourCancel = null,
        tooltipContent = tooltipContent,
        content = content,
    )
}
