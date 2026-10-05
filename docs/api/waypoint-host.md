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
    tooltipContent: @Composable (StepScope) -> Unit,
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
| `keyboardConfig` | `KeyboardConfig` | `KeyboardConfig.Default` | Keyboard navigation config (arrow keys, Enter, Escape). See [`KeyboardConfig`](#keyboardconfig). |
| `tooltipSpacing` | `Dp` | `12.dp` | Gap between the tooltip and the target. |
| `screenMargin` | `Dp` | `16.dp` | Minimum margin from screen edges before the tooltip flips or shifts. |
| `onTourComplete` | `(() -> Unit)?` | `null` | Called when a tour run ends with `WaypointEndReason.Completed`, no matter which host or code path finished it. |
| `onTourCancel` | `(() -> Unit)?` | `null` | Called when a tour run ends with `WaypointEndReason.Cancelled`: skip, Escape, overlay dismiss, or a direct `state.stop()` call. |
| `tooltipContent` | `@Composable (StepScope) -> Unit` | required | Tooltip of every step that has no `content` of its own. The [`StepScope`](#stepscope) carries the step's texts, the resolved placement, progress and navigation. |
| `content` | `@Composable () -> Unit` | required | Your screen content. |

Defaults live on `WaypointDefaults` so you can read or reuse them in your own wrappers.

## Placement rules

!!! warning
    `WaypointHost` must wrap **all** content that contains tour targets. Place it at the root of your screen.

Waypoint stores target bounds in the host's coordinate space, and the host renders the spotlight and tooltip in the same space. If a `Modifier.waypointTarget` sits outside any host, it is silently ignored (no host is in scope).

The host is also the area that a step covers: the spotlight scrim dims and blocks the host's bounds, and a step without a target is centered over them.

For tours that span a modal surface (Dialog, ModalBottomSheet, Popup), add a [`WaypointOverlayHost`](#waypointoverlayhost) inside the modal. The two hosts share the same `WaypointState`, and Waypoint renders the overlay + tooltip in whichever host owns the current step's target.

## `StepScope`

The scope handed to `tooltipContent` and to per-step `content { }`.

```kotlin
@Stable
public interface StepScope {
    public val title: String?
    public val description: String?
    public val placement: ResolvedPlacement?
    public val currentStepIndex: Int
    public val currentStepNumber: Int
    public val totalSteps: Int
    public val isFirstStep: Boolean
    public val isLastStep: Boolean
    public val advancesAutomatically: Boolean
    public fun next()
    public fun previous()
    public fun skip()
}
```

`StepScope` is implemented by the library only, it may gain members in any release.

| Member | Description |
|---|---|
| `title`, `description` | The texts configured on the current step. |
| `placement` | Resolved side of the target the tooltip sits on (`Top`, `Bottom`, `Start`, `End`), `null` for a step without a target. |
| `currentStepIndex` | Index of the step in `WaypointState.steps` (0-based, includes hidden steps). |
| `currentStepNumber` | 1-based position among currently-visible steps, for "X of Y" progress. |
| `totalSteps` | Number of currently-visible steps (steps whose `showIf` passes). |
| `isFirstStep`, `isLastStep` | Whether this is the first or last visible step. |
| `advancesAutomatically` | True when the step's `advanceOn` is armed for this visit (the step has one and was not entered with `previous()`), so the tour moves on by itself. `WaypointMaterial3Tooltip` hides Next/Finish in that case. |
| `next()` | Go to the next step, or complete the tour on the last one. |
| `previous()` | Go to the previous step. |
| `skip()` | Cancel the tour. |

## `TooltipArrowBox`

Lays out custom tooltip content with an arrow that points at the current target. Use it as the root of a custom tooltip.

```kotlin
@Composable
public fun TooltipArrowBox(
    arrowColor: Color,
    modifier: Modifier = Modifier,
    arrowSize: Dp = 10.dp,
    arrowWidth: Dp = arrowSize * 2,
    content: @Composable () -> Unit,
)
```

```kotlin
WaypointHost(
    state = tourState,
    tooltipContent = { stepScope ->
        TooltipArrowBox(arrowColor = Color.White) {
            MyTooltipCard(stepScope)
        }
    },
) { MyScreen() }
```

It renders the bare content for a step without a target. For fully custom arrow drawing, `TooltipArrow(placement, color, modifier, size, arrowWidth)` and `LocalTooltipArrowGeometry` are public too. See [Custom Tooltips](../guides/custom-tooltips.md#arrows).

## `OverlayClickBehavior`

Controls what happens when the user taps the blocked area outside the spotlight cutout. Only `HighlightStyle.Spotlight` blocks touches, so it has no effect with the other highlight styles.

| Value | Behavior |
|---|---|
| `OverlayClickBehavior.Nothing` | Absorb the click, do nothing (default). |
| `OverlayClickBehavior.Dismiss` | Cancel the tour (`state.stop()`, then `onTourCancel` fires). |
| `OverlayClickBehavior.NextStep` | Advance to the next step. |
| `OverlayClickBehavior.Custom(action)` | Run a custom lambda. |

Example, tap anywhere to advance:

```kotlin
WaypointHost(
    state = tourState,
    overlayClickBehavior = OverlayClickBehavior.NextStep,
    tooltipContent = { stepScope -> MyTooltip(stepScope) },
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
    tooltipContent = { stepScope -> MyTooltip(stepScope) },
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
    tooltipContent = { stepScope -> MyTooltip(stepScope) },
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

While a step is shown the host takes keyboard focus so these keys work. During a `TargetInteraction.PassThrough` step it leaves focus to the app and only handles `dismissKeys`, because the user may be typing in the target. No key is handled while the tour is paused. See [Keyboard Navigation](../guides/keyboard.md).

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
    tooltipContent: @Composable (StepScope) -> Unit,
    content: @Composable () -> Unit,
)
```

Both hosts share the same `WaypointState`. Targets register against whichever host is their nearest ancestor, and Waypoint renders the overlay + tooltip in the host that owns the current step's target. Tour-lifecycle callbacks (`onTourComplete`, `onTourCancel`) and keyboard handling stay on the primary host, and the callbacks fire for every way the tour ends, including tooltip buttons inside a `WaypointOverlayHost`. Steps without a target are always shown by the primary host.

```kotlin
WaypointHost(state = state, tooltipContent = { MyTooltip(it) }) {
    MyScreen()

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            WaypointOverlayHost(state = state, tooltipContent = { MyTooltip(it) }) {
                DialogContent() // contains waypointTarget modifiers
            }
        }
    }
}
```

## `LocalWaypointHostId`

`LocalWaypointHostId` is a `CompositionLocal` that identifies the nearest host in the composition. `Modifier.waypointTarget` reads it to decide which host's coordinate space the target registers in. You only need to read it yourself when registering bounds manually with the experimental `WaypointState.setTargetBounds`. Otherwise it's mentioned here so the mechanism is explicit if you're debugging target registration across Dialog / Sheet boundaries.

## Complete examples

### Minimal host

```kotlin
WaypointHost(
    state = tourState,
    tooltipContent = { stepScope -> MyTooltip(stepScope) },
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
    tooltipContent = { stepScope -> MyTooltip(stepScope) },
) {
    MyScreen(tourState)
}
```

### Host with keyboard disabled

```kotlin
WaypointHost(
    state = tourState,
    keyboardConfig = KeyboardConfig.Disabled,
    tooltipContent = { stepScope -> MyTooltip(stepScope) },
) {
    MyScreen(tourState)
}
```

## See also

- [WaypointState API](waypoint-state.md), the state holder and DSL
- [Material3 API](material3.md), pre-styled tooltip wrapper
- [Highlight Styles](../guides/highlight-styles.md), configure the visual emphasis
- [Custom Tooltips](../guides/custom-tooltips.md), build your own `tooltipContent`
- [Interactive Tutorials](../guides/interactive-tutorials.md), steps the user works in
