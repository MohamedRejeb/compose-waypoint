# Theming

`WaypointMaterial3Theme` provides a CompositionLocal-based theming layer for the Material3 tooltip (and hint tooltip). It lets you override colors, typography, and dimensions without touching tooltip code.

By default, theme values derive from `MaterialTheme.colorScheme` and `MaterialTheme.typography`, so a tour inherits your app's look automatically. You only override what you want to change.

## Setup

Wrap your Waypoint host (or the whole screen) in `WaypointMaterial3Theme`. Any `WaypointMaterial3Tooltip` or `WaypointMaterial3HintTooltip` underneath reads from the theme.

```kotlin
WaypointMaterial3Theme(
    colors = WaypointMaterial3Theme.colors(
        tooltipBackground = Color(0xFF1B1B2F),
        title = Color.White,
        description = Color.White.copy(alpha = 0.8f),
        primaryButton = Color(0xFF7C4DFF),
    ),
) {
    WaypointMaterial3Host(state = tourState) {
        MyScreen()
    }
}
```

!!! tip
    You don't have to wrap with `WaypointMaterial3Theme` at all. Omit it and the tooltip uses Material3 defaults pulled from `MaterialTheme`. Only add the wrapper when you want to diverge from those defaults.

## What you can override

`WaypointMaterial3Theme` composes three independent pieces. Each is optional, so you can theme colors without touching typography (and vice versa).

| Section | Type | Purpose |
|---|---|---|
| `colors` | `WaypointMaterial3Colors` | Surface color, text colors, button colors. |
| `typography` | `WaypointMaterial3Typography` | Text styles for title, description, progress, button. |
| `dimensions` | `WaypointMaterial3Dimensions` | Shape, width bounds, padding, elevation, spacing. |

### Colors

```kotlin
WaypointMaterial3Theme.colors(
    tooltipBackground = MaterialTheme.colorScheme.surface,
    title = MaterialTheme.colorScheme.onSurface,
    description = MaterialTheme.colorScheme.onSurfaceVariant,
    progress = MaterialTheme.colorScheme.onSurfaceVariant,
    primaryButton = MaterialTheme.colorScheme.primary,
    secondaryButton = MaterialTheme.colorScheme.primary,
    skipButton = MaterialTheme.colorScheme.onSurfaceVariant,
)
```

| Field | Applied to |
|---|---|
| `tooltipBackground` | Tooltip surface fill. |
| `title` | Title text color. |
| `description` | Description text color. |
| `progress` | "1 of 3" progress label color. |
| `primaryButton` | Next / Finish button text. |
| `secondaryButton` | Back button text. |
| `skipButton` | Skip button text. |

### Typography

```kotlin
WaypointMaterial3Theme.typography(
    title = MaterialTheme.typography.titleMedium,
    description = MaterialTheme.typography.bodyMedium,
    progress = MaterialTheme.typography.labelSmall,
    button = MaterialTheme.typography.labelLarge,
)
```

Use any `TextStyle`, the same way you do elsewhere in Material3.

### Dimensions

```kotlin
WaypointMaterial3Theme.dimensions(
    tooltipShape = RoundedCornerShape(16.dp),
    tooltipMinWidth = 200.dp,
    tooltipMaxWidth = 320.dp,
    tooltipPadding = 20.dp,
    tooltipElevation = 8.dp,
    contentSpacing = 8.dp,
)
```

| Field | Default | Purpose |
|---|---|---|
| `tooltipShape` | `RoundedCornerShape(16.dp)` | Tooltip outer shape (clips + shadows). |
| `tooltipMinWidth` | `200.dp` | Minimum tooltip width. |
| `tooltipMaxWidth` | `320.dp` | Maximum tooltip width. |
| `tooltipPadding` | `20.dp` | Inner padding around content. |
| `tooltipElevation` | `8.dp` | Shadow elevation. |
| `contentSpacing` | `8.dp` | Vertical spacing between title/description/buttons. |

## Full example

A brand-themed tour with a custom accent color and rounder tooltip:

```kotlin
@Composable
fun BrandedTour() {
    val tourState = rememberWaypointState {
        step(Targets.Search) {
            title = "Lightning-fast search"
            description = "Find anything with Cmd+K."
        }
        step(Targets.Create) {
            title = "Create in one tap"
            description = "The big plus button is always available."
        }
    }

    WaypointMaterial3Theme(
        colors = WaypointMaterial3Theme.colors(
            tooltipBackground = Color(0xFF120B1E),
            title = Color(0xFFEDE7FF),
            description = Color(0xFFBFB3E6),
            primaryButton = Color(0xFFA594FF),
            secondaryButton = Color(0xFF7C4DFF),
            skipButton = Color(0xFF8074A8),
            progress = Color(0xFF8074A8),
        ),
        typography = WaypointMaterial3Theme.typography(
            title = MaterialTheme.typography.titleLarge,
        ),
        dimensions = WaypointMaterial3Theme.dimensions(
            tooltipShape = RoundedCornerShape(24.dp),
            tooltipPadding = 24.dp,
            tooltipElevation = 16.dp,
        ),
    ) {
        WaypointMaterial3Host(state = tourState) {
            MyScreen(tourState)
        }
    }
}
```

## Per-step style overrides

Theming controls the tooltip's global appearance. For per-step tweaks, use `content { stepScope -> ... }` on the step to render a completely different composable. That step bypasses the Material3 tooltip entirely, which means it also bypasses the theme, so decide whether you want to read `WaypointMaterial3Theme.colors` manually inside your custom content.

```kotlin
step(Targets.Special) {
    content { stepScope ->
        val colors = WaypointMaterial3Theme.colors
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(colors.tooltipBackground)
                .padding(24.dp),
        ) {
            Text("Special step", color = colors.title)
            TextButton(onClick = { stepScope.next() }) {
                Text("Continue", color = colors.primaryButton)
            }
        }
    }
}
```

## Light vs dark mode

`WaypointMaterial3Theme.colors()` resolves every value against `MaterialTheme.colorScheme` when called without overrides. This means if you wrap a single `WaypointMaterial3Theme { ... }` block, it automatically follows your app's light/dark toggle for any field you don't pin to a fixed color.

To pin a color only in dark mode, read `isSystemInDarkTheme()` and switch:

```kotlin
val isDark = isSystemInDarkTheme()
WaypointMaterial3Theme(
    colors = WaypointMaterial3Theme.colors(
        tooltipBackground = if (isDark) Color(0xFF1B1B2F) else Color.White,
    ),
) { ... }
```

## See also

- [Material3 API](../api/material3.md), the full `WaypointMaterial3Host` / `WaypointMaterial3Tooltip` reference
- [Custom Tooltips](custom-tooltips.md), when theming isn't enough
- [Highlight Styles](highlight-styles.md), control what surrounds the target
