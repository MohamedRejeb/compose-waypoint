# WaypointHost

`WaypointHost<K>` is the composable that wraps your screen content and renders the tour overlay on top. It owns the spotlight highlight, the tooltip popup, keyboard handling, and the tour-lifecycle callbacks.

For a pre-styled alternative, see the [Material3 API](material3.md). Use `WaypointOverlayHost` when your tour spans a Dialog, Sheet, or Popup.

## Signature

```kotlin
@Composable
public fun <K> WaypointHost(
    state: WaypointState<K>,
    modifier: Modifier = Modifier,
    highlightStyle: HighlightStyle = WaypointDefaults.HighlightStyle,
    overlayClickBehavior: OverlayClickBehavior = WaypointDefaults.OverlayClickBehavior,
    keyboardConfig: KeyboardConfig = WaypointDefaults.KeyboardConfig,
    tooltipSpacing: Dp = WaypointDefaults.TooltipSpacing,
    screenMargin: Dp = WaypointDefaults.ScreenMargin,
    onTourComplete: (() -> Unit)? = null,
    onTourCancel: (() -> Unit)? = null,
    tooltipContent: @Composable (StepScope, ResolvedPlacement) -> Unit,
    content: @Composable () -> Unit,
)
```

## Parameters

| Parameter | Type | Default | Purpose |
|---|---|---|---|
| `state` | `WaypointState<K>` | required | The tour state holder created via [`rememberWaypointState`](waypoint-state.md). |
| `modifier` | `Modifier` | `Modifier` | Applied to the host's outer `Box`. |
| `highlightStyle` | `HighlightStyle` | `HighlightStyle.Spotlight()` | Default highlight for every step. Steps override via `step { highlightStyle = ... }`. |
| `overlayClickBehavior` | `OverlayClickBehavior` | `Nothing` | What happens when the user clicks outside the highlighted target. Only applies to the `Spotlight` highlight style. |
| `keyboardConfig` | `KeyboardConfig` | `KeyboardConfig.Default` | Keyboard navigation config (arrow keys, Enter, Escape). |
| `tooltipSpacing` | `Dp` | `12.dp` | Gap between the tooltip and the target. |
| `screenMargin` | `Dp` | `16.dp` | Minimum margin from screen edges before the tooltip flips or shifts. |
| `onTourComplete` | `(() -> Unit)?` | `null` | Called when the user advances past the last step. |
| `onTourCancel` | `(() -> Unit)?` | `null` | Called when the user skips or the tour is stopped mid-flight. |
| `tooltipContent` | `@Composable (StepScope, ResolvedPlacement) -> Unit` | required | Slot for your tooltip composable. See [Custom Tooltips](../guides/custom-tooltips.md). |
| `content` | `@Composable () -> Unit` | required | Your screen content. |

Defaults live on `WaypointDefaults` so you can read or reuse them in your own wrappers.

## Placement rules

!!! warning
    `WaypointHost` must wrap **all** content that contains tour targets. Place it at the root of your screen.

Waypoint reads target bounds via `onGloballyPositioned` and stores them in the host's coordinate space. The host then renders the spotlight and tooltip in the same space. If a `Modifier.waypointTarget` sits outside any host, it is silently ignored (no host is in scope).

For tours that span a modal surface (Dialog, ModalBottomSheet, Popup), add a [`WaypointOverlayHost`](#waypointoverlayhost) inside the modal. The two hosts share the same `WaypointState`, and Waypoint renders the overlay + tooltip in whichever host owns the current step's target.

## `OverlayClickBehavior`

Controls what happens when the user taps the darkened area outside the spotlight cutout.

| Value | Behavior |
|---|---|
| `OverlayClickBehavior.Nothing` | Absorb the click, do nothing (default). |
| `OverlayClickBehavior.Dismiss` | Cancel the tour (`state.stop()` + `onTourCancel`). |
| `OverlayClickBehavior.NextStep` | Advance to the next step. |
| `OverlayClickBehavior.Custom(action)` | Run a custom lambda. |

Example, tap anywhere to advance:

```kotlin
WaypointHost(
    state = tourState,
    overlayClickBehavior = OverlayClickBehavior.NextStep,
    tooltipContent = { s, p -> MyTooltip(s, p) },
) { MyScreen() }
```

Example, tap outside to quietly stash the tour:

```kotlin
WaypointHost(
    state = tourState,
    overlayClickBehavior = OverlayClickBehavior.Custom {
        tourState.pause()
        showResumeBanner = true
    },
    tooltipContent = { s, p -> MyTooltip(s, p) },
) { MyScreen() }
```

## `KeyboardConfig`

On Desktop and Web, users navigate tours with the keyboard. `KeyboardConfig` controls the key bindings.

```kotlin
@Immutable
public data class KeyboardConfig(
    val nextKeys: Set<Key> = setOf(Key.DirectionRight, Key.Enter),
    val previousKeys: Set<Key> = setOf(Key.DirectionLeft),
    val dismissKeys: Set<Key> = setOf(Key.Escape),
    val enabled: Boolean = true,
)
```

| Field | Default | Purpose |
|---|---|---|
| `nextKeys` | `→`, `Enter` | Advance to the next step. |
| `previousKeys` | `←` | Go back one step. |
| `dismissKeys` | `Escape` | Cancel the tour. |
| `enabled` | `true` | Master toggle. Set `false` to opt out entirely. |

Disable keyboard navigation:

```kotlin
WaypointHost(
    state = tourState,
    keyboardConfig = KeyboardConfig.Disabled,
    tooltipContent = { s, p -> MyTooltip(s, p) },
) { MyScreen() }
```

Custom bindings:

```kotlin
keyboardConfig = KeyboardConfig(
    nextKeys = setOf(Key.Spacebar, Key.Enter),
    previousKeys = setOf(Key.Backspace),
    dismissKeys = setOf(Key.Escape, Key.Q),
)
```

!!! note
    Keyboard handling is only owned by the primary `WaypointHost`. `WaypointOverlayHost` intentionally doesn't install key handlers, otherwise they'd fire twice when a modal is open.

## `WaypointOverlayHost`

Secondary host for cross-hierarchy tours. Place it inside a Dialog, Sheet, or Popup to let a tour managed by the outer `WaypointHost` reach into that modal's composition tree.

```kotlin
@Composable
public fun <K> WaypointOverlayHost(
    state: WaypointState<K>,
    modifier: Modifier = Modifier,
    highlightStyle: HighlightStyle = WaypointDefaults.HighlightStyle,
    overlayClickBehavior: OverlayClickBehavior = WaypointDefaults.OverlayClickBehavior,
    tooltipSpacing: Dp = WaypointDefaults.TooltipSpacing,
    screenMargin: Dp = WaypointDefaults.ScreenMargin,
    tooltipContent: @Composable (StepScope, ResolvedPlacement) -> Unit,
    content: @Composable () -> Unit,
)
```

Both hosts share the same `WaypointState`. Targets register against whichever host is their nearest ancestor, and Waypoint renders the overlay + tooltip in the host that owns the current step's target. Tour-lifecycle callbacks (`onTourComplete`, `onTourCancel`, keyboard) stay on the primary host.

```kotlin
WaypointHost(state = state, tooltipContent = { s, p -> MyTooltip(s, p) }) {
    MyScreen()

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            WaypointOverlayHost(state = state, tooltipContent = { s, p -> MyTooltip(s, p) }) {
                DialogContent() // contains waypointTarget modifiers
            }
        }
    }
}
```

## `LocalWaypointHostId`

`LocalWaypointHostId` is an internal `CompositionLocal` that identifies the nearest host in the composition. `Modifier.waypointTarget` reads it to decide which host's coordinate space the target registers in. You don't need to interact with it directly, it's mentioned here so the mechanism is explicit if you're debugging target registration across Dialog / Sheet boundaries.

## Complete examples

### Minimal host

```kotlin
WaypointHost(
    state = tourState,
    tooltipContent = { stepScope, placement ->
        MyTooltip(stepScope, placement)
    },
) {
    MyScreen(tourState)
}
```

### Host with custom overlay click behavior

```kotlin
WaypointHost(
    state = tourState,
    overlayClickBehavior = OverlayClickBehavior.NextStep,
    onTourComplete = { analytics.log("tour_finished") },
    tooltipContent = { stepScope, placement ->
        MyTooltip(stepScope, placement)
    },
) {
    MyScreen(tourState)
}
```

### Host with keyboard disabled

```kotlin
WaypointHost(
    state = tourState,
    keyboardConfig = KeyboardConfig.Disabled,
    tooltipContent = { stepScope, placement ->
        MyTooltip(stepScope, placement)
    },
) {
    MyScreen(tourState)
}
```

## See also

- [WaypointState API](waypoint-state.md), the state holder and DSL
- [Material3 API](material3.md), pre-styled tooltip wrapper
- [Highlight Styles](../guides/highlight-styles.md), configure the visual emphasis
- [Custom Tooltips](../guides/custom-tooltips.md), build your own `tooltipContent`
