package com.mohamedrejeb.waypoint.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * Wraps [content] and draws an animated beacon indicator at the specified [alignment].
 *
 * Beacons are standalone visual indicators — they work independently from tours
 * and can be used as persistent hints to draw attention to a UI element.
 *
 * ```kotlin
 * WaypointBeacon(
 *     visible = !hasSeenFeature,
 *     style = BeaconStyle.Pulse(color = Color.Red),
 *     onClick = { hasSeenFeature = true },
 * ) {
 *     SettingsButton()
 * }
 * ```
 *
 * @param visible whether the beacon is visible (animates in/out)
 * @param style visual style (Pulse or Dot)
 * @param alignment where the beacon is positioned relative to the content
 * @param offset additional offset from the alignment position
 * @param onClick optional click handler for the beacon
 * @param modifier modifier for the outer Box
 * @param content the UI element to decorate with the beacon
 */
@Composable
public fun WaypointBeacon(
    visible: Boolean = true,
    style: BeaconStyle = BeaconStyle.Pulse(),
    alignment: Alignment = Alignment.TopEnd,
    offset: DpOffset = DpOffset.Zero,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(alignment)
                .offset(x = offset.x, y = offset.y),
        ) {
            BeaconIndicator(
                style = style,
                onClick = onClick,
            )
        }
    }
}

@Composable
internal fun BeaconIndicator(
    style: BeaconStyle,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    when (style) {
        is BeaconStyle.Pulse -> PulseBeaconCanvas(style, onClick, modifier)
        is BeaconStyle.Dot -> DotBeaconCanvas(style, onClick, modifier)
    }
}

@Composable
private fun PulseBeaconCanvas(
    style: BeaconStyle.Pulse,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val progress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    val density = LocalDensity.current
    val beaconRadiusPx = with(density) { style.beaconRadius.toPx() }
    val maxPulseRadiusPx = with(density) { style.maxPulseRadius.toPx() }
    val canvasSize = style.maxPulseRadius * 2

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Canvas(
        modifier = modifier
            .size(canvasSize)
            .then(clickModifier),
    ) {
        // Expanding pulse ring (fades out as it grows)
        val currentPulseRadius = beaconRadiusPx +
            (maxPulseRadiusPx - beaconRadiusPx) * progress.value
        val pulseAlpha = 0.6f * (1f - progress.value)
        drawCircle(
            color = style.color.copy(alpha = pulseAlpha),
            radius = currentPulseRadius,
            center = center,
        )

        // Solid center dot
        drawCircle(
            color = style.color,
            radius = beaconRadiusPx,
            center = center,
        )
    }
}

@Composable
private fun DotBeaconCanvas(
    style: BeaconStyle.Dot,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val radiusPx = with(density) { style.radius.toPx() }
    val canvasSize = style.radius * 2

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Canvas(
        modifier = modifier
            .size(canvasSize)
            .then(clickModifier),
    ) {
        drawCircle(
            color = style.color,
            radius = radiusPx,
            center = center,
        )
    }
}
