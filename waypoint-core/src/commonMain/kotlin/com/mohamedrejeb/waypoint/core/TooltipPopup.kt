package com.mohamedrejeb.waypoint.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
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

    // Read in the host's window: inside the popup the window info is the popup's own.
    val maxContentSize = maxTooltipSize(screenMargin)

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = null,
    ) {
        TooltipFrame(maxContentSize) {
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
    screenMargin: Float,
    content: @Composable () -> Unit,
) {
    val maxContentSize = maxTooltipSize(screenMargin)
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = null,
    ) {
        TooltipFrame(maxContentSize) {
            CompositionLocalProvider(LocalTooltipArrowGeometry provides null) {
                content()
            }
        }
    }
}

/**
 * The largest size tooltip content may take: the host's window minus the
 * screen margin on every side, so a long tooltip never runs past the margin.
 */
@Composable
private fun maxTooltipSize(screenMargin: Float): DpSize {
    val window = LocalWindowInfo.current.containerSize
    return with(LocalDensity.current) {
        DpSize(
            width = (window.width - 2 * screenMargin).coerceAtLeast(0f).toDp(),
            height = (window.height - 2 * screenMargin).coerceAtLeast(0f).toDp(),
        )
    }
}

/**
 * Shared tooltip chrome: the size limit, the enter transition and the live
 * region that makes screen readers announce a newly shown tooltip.
 */
@Composable
private fun TooltipFrame(
    maxContentSize: DpSize,
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
            modifier = Modifier
                .sizeIn(maxWidth = maxContentSize.width, maxHeight = maxContentSize.height)
                .semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            content()
        }
    }
}
