package com.mohamedrejeb.waypoint.sample.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing and radii shared by both themes. */
object Dimens {
    val ScreenPadding: Dp = 20.dp
    val ContentMaxWidth: Dp = 560.dp
    val RadiusLarge: Dp = 24.dp
    val RadiusCard: Dp = 20.dp
    val RadiusRow: Dp = 18.dp
}

object SampleTheme {
    val colors: SampleColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSampleColors.current
}

/**
 * Provides the sample's tokens. MaterialTheme is fed from the same tokens so
 * the few Material3 primitives the kit wraps, and the Material3 tooltip shown
 * in the Lab, match the rest of the app.
 */
@Composable
fun SampleTheme(
    dark: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (dark) DarkSampleColors else LightSampleColors
    CompositionLocalProvider(
        LocalSampleColors provides colors,
        LocalContentColor provides colors.ink,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialScheme(),
            typography = SampleTypography(),
            content = content,
        )
    }
}

private fun SampleColors.toMaterialScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = surfaceTint,
        onPrimaryContainer = ink,
        secondary = accent,
        onSecondary = onAccent,
        background = background,
        onBackground = ink,
        surface = background,
        onSurface = ink,
        surfaceVariant = surface,
        onSurfaceVariant = inkMuted,
        surfaceContainerLowest = background,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = surface,
        surfaceContainerHighest = surface,
        outline = outline,
        outlineVariant = outline,
        scrim = scrim,
    )
}
