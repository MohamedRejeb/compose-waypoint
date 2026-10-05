package com.mohamedrejeb.waypoint.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
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
    // provider is keyed on targetBounds, which changes whenever the target
    // moves). Seeding the new provider avoids a one-frame flicker back to the
    // default placement before its first layout pass.
    val lastResolved = remember { arrayOf(ResolvedPlacement.Bottom) }

    val arrowEdgeInsetPx = with(LocalDensity.current) { ArrowEdgeInset.toPx() }
    val positionProvider = remember(targetBounds, placement, tooltipSpacing, screenMargin, arrowEdgeInsetPx) {
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

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = null,
    ) {
        TooltipFrame {
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
                content(resolvedPlacement)
            }
        }
    }
}

/**
 * Renders the tooltip of a step without a target as a Popup centered over the
 * layout it is composed in. There is nothing to point at, so no arrow geometry
 * is provided.
 */
@Composable
internal fun CenteredTooltipPopup(
    content: @Composable () -> Unit,
) {
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = null,
    ) {
        TooltipFrame {
            CompositionLocalProvider(LocalTooltipArrowGeometry provides null) {
                content()
            }
        }
    }
}

/**
 * Shared tooltip chrome: the enter transition and the live region that makes
 * screen readers announce a newly shown tooltip.
 */
@Composable
private fun TooltipFrame(
    content: @Composable () -> Unit,
) {
    // Real enter transition: starts invisible, animates in on first frame.
    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = true

    // Only an enter transition: the popup leaves the composition when its
    // step ends, so an exit transition would never play.
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn() + slideInVertically { it / 4 },
    ) {
        Box(
            modifier = Modifier.semantics {
                liveRegion = LiveRegionMode.Polite
            },
        ) {
            content()
        }
    }
}
