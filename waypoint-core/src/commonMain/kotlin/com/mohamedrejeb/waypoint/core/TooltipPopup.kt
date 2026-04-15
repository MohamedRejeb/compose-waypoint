package com.mohamedrejeb.waypoint.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup

/**
 * Renders the tooltip as a Popup positioned near the target.
 */
@Composable
internal fun TooltipPopup(
    visible: Boolean,
    targetBounds: Rect,
    placement: TooltipPlacement,
    tooltipSpacing: Float,
    screenMargin: Float,
    content: @Composable (ResolvedPlacement) -> Unit,
) {
    val positionProvider = remember(targetBounds, placement, tooltipSpacing, screenMargin) {
        WaypointPositionProvider(
            targetBounds = targetBounds,
            requestedPlacement = placement,
            spacingPx = tooltipSpacing,
            screenMarginPx = screenMargin,
        )
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = null,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { it / 4 },
            exit = fadeOut() + slideOutVertically { it / 4 },
        ) {
            Box(
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                },
            ) {
                content(positionProvider.resolvedPlacement)
            }
        }
    }
}
