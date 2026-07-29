package com.mohamedrejeb.waypoint.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup

/**
 * Minimum distance between the arrow center and the tooltip corners, keeping
 * the arrow off typical rounded corners (16dp radius + half the arrow width).
 */
private val ArrowEdgeInset = 26.dp

/**
 * Renders the tooltip as a Popup positioned near the target.
 */
@Composable
internal fun TooltipPopup(
    targetBounds: Rect,
    placement: TooltipPlacement,
    tooltipSpacing: Float,
    screenMargin: Float,
    content: @Composable (ResolvedPlacement) -> Unit,
) {
    // Carries the last resolved placement across provider recreations (the
    // provider is keyed on targetBounds, which changes every frame during the
    // bounds animation). Seeding the new provider avoids a one-frame flicker
    // back to the default placement before its first layout pass.
    val lastResolved = remember { arrayOf(ResolvedPlacement.Bottom) }

    val arrowEdgeInsetPx = with(LocalDensity.current) { ArrowEdgeInset.toPx() }
    val positionProvider = remember(targetBounds, placement, tooltipSpacing, screenMargin) {
        WaypointPositionProvider(
            targetBounds = targetBounds,
            requestedPlacement = placement,
            spacingPx = tooltipSpacing,
            screenMarginPx = screenMargin,
            initialPlacement = lastResolved[0],
            arrowEdgeInsetPx = arrowEdgeInsetPx,
        )
    }
    SideEffect { lastResolved[0] = positionProvider.resolvedPlacement }

    // Real enter transition: starts invisible, animates in on first frame.
    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = true

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = null,
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn() + slideInVertically { it / 4 },
            exit = fadeOut() + slideOutVertically { it / 4 },
        ) {
            val resolvedPlacement = positionProvider.resolvedPlacement
            val geometry = TooltipArrowGeometry(
                placement = resolvedPlacement,
                arrowOffset = when (resolvedPlacement) {
                    ResolvedPlacement.Top, ResolvedPlacement.Bottom ->
                        positionProvider.arrowHorizontalOffset
                    ResolvedPlacement.Start, ResolvedPlacement.End ->
                        positionProvider.arrowVerticalOffset
                },
            )
            CompositionLocalProvider(LocalTooltipArrowGeometry provides geometry) {
                Box(
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                    },
                ) {
                    content(resolvedPlacement)
                }
            }
        }
    }
}
