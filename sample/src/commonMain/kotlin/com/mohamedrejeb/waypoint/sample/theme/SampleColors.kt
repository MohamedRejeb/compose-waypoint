package com.mohamedrejeb.waypoint.sample.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The sample's own color roles. Screens and kit components read these
 * instead of the Material color scheme.
 */
@Immutable
data class SampleColors(
    val background: Color,
    val surface: Color,
    val surfaceTint: Color,
    val ink: Color,
    val inkMuted: Color,
    val accent: Color,
    val onAccent: Color,
    val outline: Color,
    val tooltipContainer: Color,
    val tooltipContent: Color,
    val tooltipActionContainer: Color,
    val tooltipActionContent: Color,
    val scrim: Color,
    val scrimAlpha: Float,
    val isDark: Boolean,
)

private const val MutedAlpha = 0.55f

private val LightInk = Color(0xFF101114)
private val Cobalt = Color(0xFF2F5BFF)

val LightSampleColors = SampleColors(
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF4F5F7),
    surfaceTint = Color(0xFFEAF0FF),
    ink = LightInk,
    inkMuted = LightInk.copy(alpha = MutedAlpha),
    accent = Cobalt,
    onAccent = Color(0xFFFFFFFF),
    outline = Color(0xFFE3E5EA),
    tooltipContainer = Cobalt,
    tooltipContent = Color(0xFFFFFFFF),
    tooltipActionContainer = Color(0xFFFFFFFF),
    tooltipActionContent = Cobalt,
    scrim = LightInk,
    scrimAlpha = 0.6f,
    isDark = false,
)

private val Night = Color(0xFF0C1017)
private val DarkInk = Color(0xFFE8EDF5)
private val Lime = Color(0xFFB8F34A)

val DarkSampleColors = SampleColors(
    background = Night,
    surface = Color(0xFF131A25),
    surfaceTint = Color(0xFF1F2937),
    ink = DarkInk,
    inkMuted = DarkInk.copy(alpha = MutedAlpha),
    accent = Lime,
    onAccent = Night,
    outline = Color(0xFF1F2937),
    tooltipContainer = DarkInk,
    tooltipContent = Night,
    tooltipActionContainer = Night,
    tooltipActionContent = Lime,
    scrim = Color(0xFF04060A),
    scrimAlpha = 0.72f,
    isDark = true,
)

val LocalSampleColors = staticCompositionLocalOf { LightSampleColors }
