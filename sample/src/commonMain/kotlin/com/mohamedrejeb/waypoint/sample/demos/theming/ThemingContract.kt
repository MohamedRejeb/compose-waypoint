package com.mohamedrejeb.waypoint.sample.demos.theming

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ThemingState(
    val tooltipBackground: Color = Color(0xFF1B1B2F),
    val titleColor: Color = Color.White,
    val descriptionColor: Color = Color(0xFFB0B0B0),
    val primaryButtonColor: Color = Color(0xFF7C3AED),
    val tooltipCornerRadius: Dp = 16.dp,
    val tooltipElevation: Dp = 8.dp,
    val tooltipPadding: Dp = 20.dp,
)

sealed interface ThemingEvent {
    data class BackgroundChanged(val color: Color) : ThemingEvent
    data class TitleColorChanged(val color: Color) : ThemingEvent
    data class DescriptionColorChanged(val color: Color) : ThemingEvent
    data class ButtonColorChanged(val color: Color) : ThemingEvent
    data class CornerRadiusChanged(val dp: Dp) : ThemingEvent
    data class ElevationChanged(val dp: Dp) : ThemingEvent
    data class PaddingChanged(val dp: Dp) : ThemingEvent
    data object ResetDefaults : ThemingEvent
}
