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
 * callbacks, those responsibilities stay on the primary host. Steps without a
 * target are always shown by the primary host.
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
 * @param blockOutside whether pointer input outside the highlighted areas is blocked (overridable per-step)
 * @param overlayClickBehavior what happens when a blocked area is tapped
 * @param tooltipSpacing spacing between tooltip and target
 * @param screenMargin minimum margin from screen edges for the tooltip
 * @param tooltipContent composable to render the tooltip; receives a [StepScope]
 * @param content the modal content that contains tour targets
 */
@Composable
public fun <K> WaypointOverlayHost(
    state: WaypointState<K>,
    modifier: Modifier = Modifier,
    highlightStyle: HighlightStyle = WaypointDefaults.HighlightStyle,
    blockOutside: Boolean = true,
    overlayClickBehavior: OverlayClickBehavior = WaypointDefaults.OverlayClickBehavior,
    tooltipSpacing: Dp = WaypointDefaults.TooltipSpacing,
    screenMargin: Dp = WaypointDefaults.ScreenMargin,
    tooltipContent: @Composable (StepScope) -> Unit,
    content: @Composable () -> Unit,
) {
    val hostId = remember { Any() }

    WaypointHostScope(
        state = state,
        hostId = hostId,
        isPrimary = false,
        modifier = modifier,
        highlightStyle = highlightStyle,
        blockOutside = blockOutside,
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
