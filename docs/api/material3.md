# Material3

The `waypoint-material3` module bundles `waypoint-core` with a pre-styled Material3 tooltip. If your app uses `MaterialTheme`, this is the shortest path from zero to a polished tour, drop `WaypointMaterial3Host` at the root of your screen and you're done.

Every public API in this module is a thin wrapper that delegates to a core equivalent, so any pattern you learn on core works here too.

## Module overview

| Composable / Class | Purpose |
|---|---|
| [`WaypointMaterial3Host`](#waypointmaterial3host) | Primary host with the Material3 tooltip pre-wired. |
| [`WaypointMaterial3OverlayHost`](#waypointmaterial3overlayhost) | Cross-hierarchy host (Dialog / Sheet / Popup). |
| [`WaypointMaterial3Tooltip`](#waypointmaterial3tooltip) | The default tooltip composable, usable standalone for per-step overrides. |
| [`WaypointMaterial3Theme`](#waypointmaterial3theme) | CompositionLocal-based theming. See [Theming](../guides/theming.md). |
| [`WaypointMaterial3Hint`](#waypointmaterial3hint) | Convenience wrapper for single-shot hints. |
| [`WaypointMaterial3HintTooltip`](#waypointmaterial3hinttooltip) | Default Material3 hint tooltip. |

## `WaypointMaterial3Host`

Convenience wrapper around [`WaypointHost`](waypoint-host.md) that plugs in `WaypointMaterial3Tooltip` as the tooltip content.

```kotlin
@Composable
public fun <K> WaypointMaterial3Host(
    state: WaypointState<K>,
    modifier: Modifier = Modifier,
    highlightStyle: HighlightStyle = WaypointDefaults.HighlightStyle,
    overlayClickBehavior: OverlayClickBehavior = WaypointDefaults.OverlayClickBehavior,
    keyboardConfig: KeyboardConfig = WaypointDefaults.KeyboardConfig,
    tooltipSpacing: Dp = WaypointDefaults.TooltipSpacing,
    screenMargin: Dp = WaypointDefaults.ScreenMargin,
    onTourComplete: (() -> Unit)? = null,
    onTourCancel: (() -> Unit)? = null,
    skipText: String = "Skip",
    nextText: String = "Next",
    backText: String = "Back",
    finishText: String = "Finish",
    showProgress: Boolean = true,
    content: @Composable () -> Unit,
)
```

### Parameters

Everything on `WaypointHost` plus the following tooltip text customization:

| Parameter | Type | Default | Purpose |
|---|---|---|---|
| `skipText` | `String` | `"Skip"` | Label for the bottom-left skip button. |
| `nextText` | `String` | `"Next"` | Label for the next button on non-final steps. |
| `backText` | `String` | `"Back"` | Label for the back button (hidden on the first step). |
| `finishText` | `String` | `"Finish"` | Label for the next button on the last step. |
| `showProgress` | `Boolean` | `true` | Toggle the "N of M" progress indicator above the title. |

For `highlightStyle`, `overlayClickBehavior`, `keyboardConfig`, `tooltipSpacing`, `screenMargin`, `onTourComplete`, and `onTourCancel`, see the [`WaypointHost` reference](waypoint-host.md).

### Minimal setup

```kotlin
enum class Targets { Search, Add, Profile }

@Composable
fun HomeScreen() {
    val tourState = rememberWaypointState<Targets> {
        step(Targets.Search) { title = "Search"; description = "Find anything fast." }
        step(Targets.Add) { title = "Create"; description = "Add new items anywhere." }
        step(Targets.Profile) { title = "Profile"; description = "Your account lives here." }
    }

    LaunchedEffect(Unit) { tourState.start() }

    WaypointMaterial3Host(state = tourState) {
        MyScreen(tourState)
    }
}
```

### Custom button text

```kotlin
WaypointMaterial3Host(
    state = tourState,
    skipText = "Maybe later",
    nextText = "Got it",
    backText = "Previous",
    finishText = "Let's go",
    showProgress = false,
) {
    MyScreen(tourState)
}
```

### Localization

`skipText`, `nextText`, `backText`, and `finishText` are plain `String` parameters, pass `stringResource(R.string.tour_next)` on Android or your own resource system on KMP.

```kotlin
WaypointMaterial3Host(
    state = tourState,
    skipText = stringResource(Res.string.tour_skip),
    nextText = stringResource(Res.string.tour_next),
    backText = stringResource(Res.string.tour_back),
    finishText = stringResource(Res.string.tour_finish),
) { MyScreen() }
```

## `WaypointMaterial3OverlayHost`

Convenience wrapper around [`WaypointOverlayHost`](waypoint-host.md#waypointoverlayhost). Use it inside a `Dialog`, `ModalBottomSheet`, or `Popup` so a tour driven by an outer `WaypointMaterial3Host` can continue targeting elements in the modal.

```kotlin
@Composable
public fun <K> WaypointMaterial3OverlayHost(
    state: WaypointState<K>,
    modifier: Modifier = Modifier,
    highlightStyle: HighlightStyle = WaypointDefaults.HighlightStyle,
    overlayClickBehavior: OverlayClickBehavior = WaypointDefaults.OverlayClickBehavior,
    tooltipSpacing: Dp = WaypointDefaults.TooltipSpacing,
    screenMargin: Dp = WaypointDefaults.ScreenMargin,
    skipText: String = "Skip",
    nextText: String = "Next",
    backText: String = "Back",
    finishText: String = "Finish",
    showProgress: Boolean = true,
    content: @Composable () -> Unit,
)
```

Notice there's no `keyboardConfig`, `onTourComplete`, or `onTourCancel`, those responsibilities belong to the primary host. The primary host's callbacks still fire when the tour completes or is cancelled from a tooltip inside the overlay host.

```kotlin
WaypointMaterial3Host(state = state) {
    MyScreen()

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            WaypointMaterial3OverlayHost(state = state) {
                DialogContent() // waypointTarget modifiers inside
            }
        }
    }
}
```

## `WaypointMaterial3Tooltip`

The default tooltip composable. `WaypointMaterial3Host` calls this for every step, but you can also drop it into a per-step `content { ... }` override when you want most steps to match the default style but one step to render custom extras.

```kotlin
@Composable
public fun WaypointMaterial3Tooltip(
    stepScope: StepScope,
    resolvedPlacement: ResolvedPlacement,
    title: String?,
    description: String?,
    modifier: Modifier = Modifier,
    skipText: String = "Skip",
    nextText: String = "Next",
    backText: String = "Back",
    finishText: String = "Finish",
    showProgress: Boolean = true,
    showArrow: Boolean = true,
)
```

Reads colors, typography, and dimensions from [`WaypointMaterial3Theme`](#waypointmaterial3theme). When composed inside a Waypoint tooltip popup, an arrow pointing at the target is drawn automatically; pass `showArrow = false` to disable it. The progress label shows `currentStepNumber of totalSteps`, counting only visible steps.

```kotlin
step(Targets.Special) {
    content { stepScope ->
        Column {
            AsyncImage(model = "https://...", contentDescription = null)
            WaypointMaterial3Tooltip(
                stepScope = stepScope,
                resolvedPlacement = ResolvedPlacement.Bottom,
                title = "Visual step",
                description = "Extra content above the default tooltip body.",
            )
        }
    }
}
```

## `WaypointMaterial3Theme`

CompositionLocal-based theming layer for `WaypointMaterial3Tooltip` and `WaypointMaterial3HintTooltip`. Defaults derive from `MaterialTheme.colorScheme` and `MaterialTheme.typography`.

```kotlin
WaypointMaterial3Theme(
    colors = WaypointMaterial3Theme.colors(
        tooltipBackground = Color(0xFF1B1B2F),
        title = Color.White,
    ),
) {
    WaypointMaterial3Host(state = tourState) { MyScreen() }
}
```

See the [Theming guide](../guides/theming.md) for the complete overrides reference.

## `WaypointMaterial3Hint`

Convenience wrapper for single-shot, non-sequential hints (for example, "New feature." bubbles that a user can dismiss individually). Uses `WaypointHint` under the hood and plugs in `WaypointMaterial3HintTooltip` as the default tooltip.

```kotlin
@Composable
public fun <K> WaypointMaterial3Hint(
    state: WaypointHintState<K>,
    key: K,
    modifier: Modifier = Modifier,
    tooltipSpacing: Dp = WaypointDefaults.TooltipSpacing,
    screenMargin: Dp = WaypointDefaults.ScreenMargin,
    gotItText: String = "Got it",
    showCloseButton: Boolean = false,
    closeContentDescription: String = "Close",
    content: @Composable () -> Unit,
)
```

| Parameter | Type | Default | Purpose |
|---|---|---|---|
| `state` | `WaypointHintState<K>` | required | Hint state holder (typically via `rememberWaypointHintState`). |
| `key` | `K` | required | The hint to render, must be registered in `state`. |
| `tooltipSpacing` | `Dp` | `12.dp` | Gap between tooltip and target. |
| `screenMargin` | `Dp` | `16.dp` | Minimum margin from screen edges. |
| `gotItText` | `String` | `"Got it"` | Label for the primary dismiss button. |
| `showCloseButton` | `Boolean` | `false` | Render a close (`x`) icon in the tooltip header. |
| `closeContentDescription` | `String` | `"Close"` | Accessibility label for the close icon. |

```kotlin
WaypointMaterial3Hint(
    state = hints,
    key = HintKeys.NewFeature,
    showCloseButton = true,
) {
    Icon(Icons.Default.NewReleases, contentDescription = "New")
}
```

## `WaypointMaterial3HintTooltip`

The default Material3 hint tooltip. Renders an optional title, optional description, and a single "Got it" action. Exposed publicly so you can reuse it when you supply a custom `tooltipContent` to `WaypointHint`.

```kotlin
@Composable
public fun WaypointMaterial3HintTooltip(
    hintScope: HintScope,
    resolvedPlacement: ResolvedPlacement,
    modifier: Modifier = Modifier,
    gotItText: String = "Got it",
    showCloseButton: Boolean = false,
    closeContentDescription: String = "Close",
    showArrow: Boolean = true,
)
```

Reads from `WaypointMaterial3Theme` the same way `WaypointMaterial3Tooltip` does, so theming applies to hints automatically. Like the tour tooltip, it draws an arrow pointing at the hint target unless `showArrow` is false.

## See also

- [WaypointHost API](waypoint-host.md), the underlying core host
- [WaypointState API](waypoint-state.md), the state holder and DSL
- [Theming](../guides/theming.md), override colors, typography, dimensions
- [Custom Tooltips](../guides/custom-tooltips.md), when the Material3 tooltip isn't enough
- [Highlight Styles](../guides/highlight-styles.md), change what surrounds the target
