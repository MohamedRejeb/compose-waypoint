package com.mohamedrejeb.waypoint.material3

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// -- Colors --

@Immutable
public data class WaypointMaterial3Colors(
    val tooltipBackground: Color,
    val title: Color,
    val description: Color,
    val progress: Color,
    val primaryButton: Color,
    val secondaryButton: Color,
    val skipButton: Color,
) {
    public companion object
}

// -- Typography --

@Immutable
public data class WaypointMaterial3Typography(
    val title: TextStyle,
    val description: TextStyle,
    val progress: TextStyle,
    val button: TextStyle,
) {
    public companion object
}

// -- Dimensions --

@Immutable
public data class WaypointMaterial3Dimensions(
    val tooltipShape: Shape,
    val tooltipMinWidth: Dp,
    val tooltipMaxWidth: Dp,
    val tooltipPadding: Dp,
    val tooltipElevation: Dp,
    val contentSpacing: Dp,
) {
    public companion object
}

// -- CompositionLocals --

internal val LocalWaypointColors = staticCompositionLocalOf<WaypointMaterial3Colors?> { null }
internal val LocalWaypointTypography = staticCompositionLocalOf<WaypointMaterial3Typography?> { null }
internal val LocalWaypointDimensions = staticCompositionLocalOf<WaypointMaterial3Dimensions?> { null }

// -- Theme composable --

/**
 * Provides theming for Waypoint Material3 tooltips.
 *
 * Wrap your `WaypointMaterial3Host` (or your screen) with this to customize
 * the tooltip appearance. Values not provided fall back to `MaterialTheme` defaults.
 *
 * ```kotlin
 * WaypointMaterial3Theme(
 *     colors = WaypointMaterial3Theme.colors(
 *         tooltipBackground = Color(0xFF1B1B2F),
 *         title = Color.White,
 *     ),
 * ) {
 *     WaypointMaterial3Host(state = waypointState) { ... }
 * }
 * ```
 */
@Composable
public fun WaypointMaterial3Theme(
    colors: WaypointMaterial3Colors? = null,
    typography: WaypointMaterial3Typography? = null,
    dimensions: WaypointMaterial3Dimensions? = null,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalWaypointColors provides colors,
        LocalWaypointTypography provides typography,
        LocalWaypointDimensions provides dimensions,
        content = content,
    )
}

/**
 * Accessors for the current Waypoint Material3 theme values.
 * Falls back to Material3 theme defaults when no explicit theme is provided.
 */
public object WaypointMaterial3Theme {

    /**
     * Current tooltip colors. Reads from [WaypointMaterial3Theme] if provided,
     * otherwise derives from [MaterialTheme].
     */
    public val colors: WaypointMaterial3Colors
        @Composable
        @ReadOnlyComposable
        get() = LocalWaypointColors.current ?: defaultColors()

    /**
     * Current tooltip typography. Reads from [WaypointMaterial3Theme] if provided,
     * otherwise derives from [MaterialTheme].
     */
    public val typography: WaypointMaterial3Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalWaypointTypography.current ?: defaultTypography()

    /**
     * Current tooltip dimensions. Reads from [WaypointMaterial3Theme] if provided,
     * otherwise uses sensible defaults.
     */
    public val dimensions: WaypointMaterial3Dimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalWaypointDimensions.current ?: defaultDimensions()

    /**
     * Create colors with Material3 defaults. Override only what you need.
     */
    @Composable
    @ReadOnlyComposable
    public fun colors(
        tooltipBackground: Color = MaterialTheme.colorScheme.surface,
        title: Color = MaterialTheme.colorScheme.onSurface,
        description: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        progress: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        primaryButton: Color = MaterialTheme.colorScheme.primary,
        secondaryButton: Color = MaterialTheme.colorScheme.primary,
        skipButton: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    ): WaypointMaterial3Colors = WaypointMaterial3Colors(
        tooltipBackground = tooltipBackground,
        title = title,
        description = description,
        progress = progress,
        primaryButton = primaryButton,
        secondaryButton = secondaryButton,
        skipButton = skipButton,
    )

    /**
     * Create typography with Material3 defaults. Override only what you need.
     */
    @Composable
    @ReadOnlyComposable
    public fun typography(
        title: TextStyle = MaterialTheme.typography.titleMedium,
        description: TextStyle = MaterialTheme.typography.bodyMedium,
        progress: TextStyle = MaterialTheme.typography.labelSmall,
        button: TextStyle = MaterialTheme.typography.labelLarge,
    ): WaypointMaterial3Typography = WaypointMaterial3Typography(
        title = title,
        description = description,
        progress = progress,
        button = button,
    )

    /**
     * Create dimensions with sensible defaults. Override only what you need.
     */
    public fun dimensions(
        tooltipShape: Shape = RoundedCornerShape(16.dp),
        tooltipMinWidth: Dp = 200.dp,
        tooltipMaxWidth: Dp = 320.dp,
        tooltipPadding: Dp = 20.dp,
        tooltipElevation: Dp = 8.dp,
        contentSpacing: Dp = 8.dp,
    ): WaypointMaterial3Dimensions = WaypointMaterial3Dimensions(
        tooltipShape = tooltipShape,
        tooltipMinWidth = tooltipMinWidth,
        tooltipMaxWidth = tooltipMaxWidth,
        tooltipPadding = tooltipPadding,
        tooltipElevation = tooltipElevation,
        contentSpacing = contentSpacing,
    )

    @Composable
    @ReadOnlyComposable
    private fun defaultColors(): WaypointMaterial3Colors = colors()

    @Composable
    @ReadOnlyComposable
    private fun defaultTypography(): WaypointMaterial3Typography = typography()

    private fun defaultDimensions(): WaypointMaterial3Dimensions = dimensions()
}
