package com.mohamedrejeb.waypoint.material3

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.waypoint.core.WaypointDefaults
import com.mohamedrejeb.waypoint.core.WaypointHint
import com.mohamedrejeb.waypoint.core.WaypointHintState

/**
 * Convenience wrapper around [WaypointHint] that plugs in
 * [WaypointMaterial3HintTooltip] as the default tooltip content.
 *
 * ```kotlin
 * WaypointMaterial3Hint(
 *     state = hints,
 *     key = MyHintKeys.NewFeature,
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
 * @param labels texts of the tooltip (got it button, close icon description)
 * @param showCloseButton whether to render a close-only button in the tooltip header
 * @param content the UI element to decorate with the hint beacon
 */
@Composable
public fun <K> WaypointMaterial3Hint(
    state: WaypointHintState<K>,
    key: K,
    modifier: Modifier = Modifier,
    tooltipSpacing: Dp = WaypointDefaults.TooltipSpacing,
    screenMargin: Dp = WaypointDefaults.ScreenMargin,
    labels: WaypointMaterial3Labels = WaypointMaterial3Labels.Default,
    showCloseButton: Boolean = false,
    content: @Composable () -> Unit,
) {
    WaypointHint(
        state = state,
        key = key,
        modifier = modifier,
        tooltipSpacing = tooltipSpacing,
        screenMargin = screenMargin,
        tooltipContent = { hintScope ->
            WaypointMaterial3HintTooltip(
                hintScope = hintScope,
                labels = labels,
                showCloseButton = showCloseButton,
            )
        },
        content = content,
    )
}
