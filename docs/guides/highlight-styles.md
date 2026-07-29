# Highlight Styles

A **highlight style** controls how the area around a tour target is visually emphasized. Waypoint ships with five built-in styles plus a fully custom escape hatch.

## Where it's applied

Highlight styles can be set at two levels:

1. **Host-wide** via `WaypointHost(highlightStyle = ...)` or `WaypointMaterial3Host(highlightStyle = ...)`. Every step inherits this style unless it overrides it.
2. **Per-step** via `highlightStyle = ...` inside a `step { }` block. When a step sets its own style, it takes precedence over the host-level one.

```kotlin
val state = rememberWaypointState {
    step(Targets.SearchBar) {
        title = "Search"
        // Inherits host highlightStyle
    }
    step(Targets.AddButton) {
        title = "Create"
        // Overrides the host default
        highlightStyle = HighlightStyle.Pulse(
            color = Color(0xFF4CAF50),
        )
    }
}

WaypointMaterial3Host(
    state = state,
    highlightStyle = HighlightStyle.Spotlight(
        shape = SpotlightShape.RoundedRect(12.dp),
    ),
) {
    MyScreen()
}
```

!!! note
    A step whose `highlightStyle` is `null` falls back to the host-level style. This is the default behavior when you omit the property. The host-level default is `WaypointDefaults.HighlightStyle`, a `Spotlight()`.

## Variants

### `Spotlight`

Dimmed overlay with a transparent cutout around the target. This is the classic product-tour look and the library default.

```kotlin
highlightStyle = HighlightStyle.Spotlight(
    shape = SpotlightShape.RoundedRect(cornerRadius = 8.dp),
    padding = SpotlightPadding(all = 4.dp),
    overlayColor = Color.Black,
    overlayAlpha = 0.6f,
    effect = SpotlightEffect.None,
)
```

| Parameter | Type | Default | Purpose |
|---|---|---|---|
| `shape` | `SpotlightShape` | `RoundedRect(8.dp)` | Geometry of the cutout. See [shapes](#shapes). |
| `padding` | `SpotlightPadding` | `4.dp` all sides | Extra space between target and cutout edge. |
| `overlayColor` | `Color` | `Color.Black` | Color of the dimmed scrim outside the cutout. |
| `overlayAlpha` | `Float` | `0.6f` | Opacity of the scrim, 0f-1f. |
| `effect` | `SpotlightEffect` | `None` | Optional glow / soft-edge / custom decoration. See [Spotlight Effects](spotlight-effects.md). |

Use Spotlight when you want to completely block distractions and focus attention on one element. It's the best fit for product onboarding and guided flows.

### `Pulse`

An animated pulsing shape around the target. No dimming overlay, the shape breathes (scales + fades) to draw the eye.

```kotlin
highlightStyle = HighlightStyle.Pulse(
    color = MaterialTheme.colorScheme.primary,
    shape = SpotlightShape.Circle,
    padding = SpotlightPadding(all = 8.dp),
    borderWidth = 3.dp,
    filled = false,
    pulseScale = 1.15f,
    durationMillis = 1200,
)
```

| Parameter | Type | Default | Purpose |
|---|---|---|---|
| `color` | `Color` | required | Color of the pulsing shape. |
| `shape` | `SpotlightShape` | `RoundedRect(8.dp)` | Shape drawn around the target. |
| `padding` | `SpotlightPadding` | `4.dp` | Gap between target and shape. |
| `borderWidth` | `Dp` | `3.dp` | Stroke width when `filled = false`. |
| `filled` | `Boolean` | `false` | If true, draws a filled shape instead of a stroke. |
| `pulseScale` | `Float` | `1.15f` | Peak scale of the outer copy. |
| `durationMillis` | `Int` | `1200` | Full pulse cycle duration. |

Use Pulse for subtle hints, feature callouts, or non-blocking tours where dimming the rest of the screen is too heavy-handed.

### `Border`

A static colored shape around the target. No animation, no overlay.

```kotlin
highlightStyle = HighlightStyle.Border(
    color = Color(0xFFFF9800),
    shape = SpotlightShape.Pill,
    padding = SpotlightPadding(horizontal = 6.dp, vertical = 4.dp),
    borderWidth = 2.dp,
    filled = false,
)
```

| Parameter | Type | Default | Purpose |
|---|---|---|---|
| `color` | `Color` | required | Border / fill color. |
| `shape` | `SpotlightShape` | `RoundedRect(8.dp)` | Shape around the target. |
| `padding` | `SpotlightPadding` | `4.dp` | Gap between target and shape. |
| `borderWidth` | `Dp` | `2.dp` | Stroke width when `filled = false`. |
| `filled` | `Boolean` | `false` | If true, draws a filled shape. |

Use Border for the quietest highlight, or when the surrounding UI is already dark and a full overlay would double up.

### `Ripple`

Expanding concentric rings radiating from the target's center.

```kotlin
highlightStyle = HighlightStyle.Ripple(
    color = MaterialTheme.colorScheme.primary,
    ringCount = 3,
    durationMillis = 2000,
    maxRadius = 60.dp,
    filled = false,
    strokeWidth = 2.dp,
)
```

| Parameter | Type | Default | Purpose |
|---|---|---|---|
| `color` | `Color` | required | Ring color. |
| `ringCount` | `Int` | `3` | Number of concurrent rings. |
| `durationMillis` | `Int` | `2000` | Time for a ring to travel from center to `maxRadius`. |
| `maxRadius` | `Dp` | `60.dp` | Outer extent of each ring. |
| `filled` | `Boolean` | `false` | If true, rings render as filled circles. |
| `strokeWidth` | `Dp` | `2.dp` | Ring stroke width, ignored when `filled` is true. |

Use Ripple to draw attention to small, discrete targets (icons, dots, FABs) where a shape-matched highlight would look cramped.

### `None`

No visual highlight. Only the tooltip is rendered.

```kotlin
highlightStyle = HighlightStyle.None
```

Use `None` for tooltip-only tours, or when you want to handle highlighting yourself (for example, a target that animates its own focus state via `onEnter`).

### `Custom`

Fully custom highlight. The composable receives both the raw target bounds and the animated (interpolated) bounds, and can render anything.

```kotlin
highlightStyle = HighlightStyle.Custom { targetBounds, animatedBounds ->
    // targetBounds: the current target's true rectangle
    // animatedBounds: tween-interpolated rectangle used for smooth transitions
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRoundRect(
            color = Color.Magenta.copy(alpha = 0.3f),
            topLeft = animatedBounds.topLeft,
            size = animatedBounds.size,
            cornerRadius = CornerRadius(16.dp.toPx()),
        )
    }
}
```

!!! tip
    Prefer `animatedBounds` when drawing so the highlight tweens smoothly between steps. Use `targetBounds` only when you need the exact, non-animated destination.

## Shapes

Every spotlight/pulse/border style accepts a `SpotlightShape`:

| Shape | Notes |
|---|---|
| `SpotlightShape.Circle` | Circle enclosing the target. Radius is half of the longer target dimension, so square targets fit snugly and wide targets get a large circle. |
| `SpotlightShape.Rect` | Rectangle matching the target bounds exactly. |
| `SpotlightShape.RoundedRect(cornerRadius: Dp)` | Rounded rectangle. Defaults to `8.dp` corner radius. This is also `SpotlightShape.Default`. |
| `SpotlightShape.Pill` | Rounded rectangle with corner radius equal to half the height. Ideal for chip- or pill-shaped controls. |

```kotlin
// Circular icon target
highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.Circle)

// Wide button that should stay rectangular
highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.RoundedRect(12.dp))

// Chip-shaped filter
highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.Pill)
```

## Padding

`SpotlightPadding` adds space between the target and the highlight edge. This is especially useful for tightly-cropped icons.

```kotlin
// Uniform
padding = SpotlightPadding(all = 8.dp)

// Horizontal vs vertical
padding = SpotlightPadding(horizontal = 12.dp, vertical = 6.dp)

// Per side
padding = SpotlightPadding(start = 4.dp, top = 8.dp, end = 4.dp, bottom = 8.dp)
```

`SpotlightPadding.Default` is `4.dp` on all sides. `SpotlightPadding.None` is zero on all sides.

## See also

- [Spotlight Effects](spotlight-effects.md), decorate the Spotlight cutout with glows, soft edges, or custom draw code
- [Custom Tooltips](custom-tooltips.md), replace the tooltip body while keeping any highlight style
- [WaypointState API](../api/waypoint-state.md), full reference for `rememberWaypointState` and step configuration
