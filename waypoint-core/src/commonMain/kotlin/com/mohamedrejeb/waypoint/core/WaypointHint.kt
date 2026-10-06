package com.mohamedrejeb.waypoint.core

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize

/**
 * A single persistent hint attached to a UI element.
 *
 * Unlike tour steps which progress sequentially, hints are independent
 * and stay visible until the user dismisses them (optionally persisted
 * via [WaypointPersistence]).
 *
 * @param K the key type identifying the hint, typically an enum
 */
@Immutable
public data class WaypointHint<K>(
    /** Unique identifier for this hint */
    val key: K,
    /** Optional title text */
    val title: String? = null,
    /** Optional description text */
    val description: String? = null,
    /** Tooltip placement relative to the hint target */
    val placement: TooltipPlacement = TooltipPlacement.Auto,
    /** Visual style for the beacon */
    val beaconStyle: BeaconStyle = BeaconStyle.Pulse(),
    /** Where the beacon is positioned relative to the target content */
    val beaconAlignment: Alignment = Alignment.TopEnd,
    /** Additional offset applied after [beaconAlignment] */
    val beaconOffset: DpOffset = DpOffset.Zero,
)

/**
 * Wraps [content] with a persistent hint beacon.
 *
 * The beacon is visible when the hint is not dismissed. Clicking it opens a
 * tooltip popup via the provided [tooltipContent]. The tooltip receives a
 * [HintScope] exposing [HintScope.dismiss] (permanent) and [HintScope.close]
 * (tooltip only).
 *
 * If [key] is not registered in [state], [content] is rendered without any
 * hint decoration.
 *
 * ```kotlin
 * WaypointHint(
 *     state = hints,
 *     key = MyHintKeys.NewFeature,
 *     tooltipContent = { scope ->
 *         MyHintCard(
 *             title = scope.title,
 *             description = scope.description,
 *             onGotIt = { scope.dismiss() },
 *         )
 *     },
 * ) {
 *     NewFeatureButton()
 * }
 * ```
 *
 * @param state the hint state holder
 * @param key the hint to render (must be registered in [state])
 * @param modifier modifier for the outer wrapper
 * @param tooltipSpacing spacing between tooltip and target
 * @param screenMargin margin from screen edges for the tooltip
 * @param tooltipContent composable for the tooltip body, receives a [HintScope]
 * @param content the UI element to decorate with the hint beacon
 */
@Composable
public fun <K> WaypointHint(
    state: WaypointHintState<K>,
    key: K,
    modifier: Modifier = Modifier,
    tooltipSpacing: Dp = WaypointDefaults.TooltipSpacing,
    screenMargin: Dp = WaypointDefaults.ScreenMargin,
    tooltipContent: @Composable (HintScope) -> Unit,
    content: @Composable () -> Unit,
) {
    val hint = state.find(key)
    if (hint == null) {
        Box(modifier = modifier) { content() }
        return
    }

    val isDismissed = state.isDismissed(key)

    if (isDismissed) {
        Box(modifier = modifier) { content() }
        return
    }

    val isOpen = state.openHintKey == key

    val density = LocalDensity.current
    val tooltipSpacingPx = with(density) { tooltipSpacing.toPx() }
    val screenMarginPx = with(density) { screenMargin.toPx() }

    // The popup is anchored to this Box, so the target is simply the Box's
    // own size; the popup's anchorBounds supply the window offset.
    var targetSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier.onSizeChanged { targetSize = it },
    ) {
        WaypointBeacon(
            visible = true,
            style = hint.beaconStyle,
            alignment = hint.beaconAlignment,
            offset = hint.beaconOffset,
            onClick = { state.open(key) },
        ) {
            content()
        }

        if (isOpen && targetSize != IntSize.Zero) {
            TooltipPopup(
                targetBounds = Rect(
                    left = 0f,
                    top = 0f,
                    right = targetSize.width.toFloat(),
                    bottom = targetSize.height.toFloat(),
                ),
                placement = hint.placement,
                tooltipSpacing = tooltipSpacingPx,
                screenMargin = screenMarginPx,
            ) { resolvedPlacement ->
                val hintScope = remember(state, key, hint.title, hint.description, resolvedPlacement) {
                    HintScopeImpl(
                        title = hint.title,
                        description = hint.description,
                        placement = resolvedPlacement,
                        onDismiss = { state.dismiss(key) },
                        onClose = { state.close() },
                    )
                }
                tooltipContent(hintScope)
            }
        }
    }
}
