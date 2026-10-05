# Keyboard Navigation

Waypoint hosts register a focusable key handler so users on Desktop and Web can navigate tours with the keyboard: arrow keys step through, Enter advances, Escape dismisses.

On Android and iOS the handler still runs, but without a hardware keyboard it has no effect. Touch navigation through the tooltip buttons remains the default.

## Core API

Pass a `KeyboardConfig` to `WaypointHost`:

```kotlin
WaypointHost(
    state = state,
    keyboardConfig = KeyboardConfig(
        nextKeys = setOf(Key.DirectionRight, Key.Enter, Key.Spacebar),
        previousKeys = setOf(Key.DirectionLeft),
        dismissKeys = setOf(Key.Escape),
    ),
    tooltipContent = { stepScope -> MyTooltip(stepScope) },
) {
    MyScreen()
}
```

`WaypointMaterial3Host` takes the same `keyboardConfig` parameter.

`KeyboardConfig` is an immutable data class:

```kotlin
@Immutable
public data class KeyboardConfig(
    val nextKeys: Set<Key> = setOf(Key.DirectionRight, Key.Enter),
    val previousKeys: Set<Key> = setOf(Key.DirectionLeft),
    val dismissKeys: Set<Key> = setOf(Key.Escape),
    val enabled: Boolean = true,
)
```

Defaults:

- **Next**: `DirectionRight`, `Enter`.
- **Previous**: `DirectionLeft`.
- **Dismiss**: `Escape`.
- **Enabled**: `true`.

Two preset instances are available on the companion:

| Preset | Purpose |
|--------|---------|
| `KeyboardConfig.Default` | Arrow keys, Enter, Escape. |
| `KeyboardConfig.Disabled` | `enabled = false`, keyboard navigation off. |

## How it works

When `enabled` is true and the tour is active, `WaypointHost` applies a modifier chain equivalent to:

```kotlin
Modifier
    .focusRequester(focusRequester)
    .onPreviewKeyEvent { event -> /* handle next/previous/dismiss */ }
    .focusable()
```

An idle host is not focusable, so it never takes part in your app's focus traversal. Once a step is on screen the host requests focus, so the keys reach it even if nothing on the screen was focused. Steps where the user works inside the target are the exception, see [Text input during a tour](#text-input-during-a-tour).

On `KeyDown` events, while the tour is active and not paused:

- Keys in `nextKeys` call `state.next()`, but only while the step is on screen (`isStepVisible`). If that was the last step, the tour completes and `onTourComplete` fires.
- Keys in `previousKeys` call `state.previous()`, under the same condition.
- Keys in `dismissKeys` call `state.stop()`, and `onTourCancel` fires. These work during any step, including one held by its `beforeShow` gate.

All other keys fall through, including your app's own shortcuts, so Waypoint doesn't swallow unrelated input. While a `beforeShow` gate holds a step, Enter and the arrows go to the app too (the user may be typing in a field), and while the tour is paused or inactive no key is handled.

!!! note
    Because `onPreviewKeyEvent` runs before descendant focus owners see the event, the host handles its keys even when a `TextField` inside it has focus. With the default config that includes Enter and the arrow keys, see [Text input during a tour](#text-input-during-a-tour).

## Disabling keyboard

```kotlin
WaypointHost(
    state = state,
    keyboardConfig = KeyboardConfig.Disabled,
    tooltipContent = { stepScope -> MyTooltip(stepScope) },
) {
    MyScreen()
}
```

When disabled, the host does not attach the key handler or take focus. The tour still runs, users navigate via tooltip buttons (Next, Back, Skip) or `advanceOn`.

## Custom shortcuts

Any combination of keys is valid. Add Page Down as a next key, or add the `Q` key as a dismiss shortcut:

```kotlin
keyboardConfig = KeyboardConfig(
    nextKeys = setOf(Key.DirectionRight, Key.Enter, Key.PageDown),
    previousKeys = setOf(Key.DirectionLeft, Key.PageUp),
    dismissKeys = setOf(Key.Escape, Key.Q),
)
```

### Dismiss-only keyboard

If you want Escape to dismiss but prefer users use the tooltip buttons for next/previous:

```kotlin
keyboardConfig = KeyboardConfig(
    nextKeys = emptySet(),
    previousKeys = emptySet(),
    dismissKeys = setOf(Key.Escape),
)
```

## Platform behavior

=== "Desktop (JVM)"

    Full keyboard navigation. Arrow keys, Enter, Space, and Escape map to Compose `Key` constants and fire `onPreviewKeyEvent` reliably.

=== "Web (Wasm/JS)"

    Works when the canvas has focus. If your app embeds Compose inside a larger HTML page, users may need to click into the canvas before arrow keys are received. Standard modifier keys (Ctrl, Cmd) pass through.

=== "Android"

    On phones, only external keyboards or game controllers deliver directional keys. The handler is still attached but effectively dormant.

=== "iOS"

    Same as Android, keys only arrive when an external keyboard is paired. Touch navigation is the primary input.

## Text input during a tour

A step that asks the user to type should use `TargetInteraction.PassThrough`. During such a step the host steps back:

- It does not take keyboard focus, so a field the user is typing in keeps it, including across the change to the next `PassThrough` step.
- It only handles `dismissKeys`. Next and previous keys (Enter and the arrows by default) go to the app.

```kotlin
step(Targets.SearchField) {
    title = "Try searching"
    interaction = TargetInteraction.PassThrough
    advanceOn { snapshotFlow { query }.first { it.isNotEmpty() } }
}
```

On every other step the host takes focus and handles all configured keys. If the user can type during such steps too (for example with a non-blocking highlight like `Pulse`, where the whole screen stays interactive), remove the keys that clash with text input or disable keyboard navigation:

```kotlin
keyboardConfig = KeyboardConfig(
    nextKeys = emptySet(),
    previousKeys = emptySet(),
)
```

The host does not move focus into the target for you. To put the cursor in a field when its step starts, request focus in `onEnter`:

```kotlin
val fieldRequester = remember { FocusRequester() }

val state = rememberWaypointState {
    step(Targets.SearchField) {
        interaction = TargetInteraction.PassThrough
        onEnter { fieldRequester.requestFocus() }
    }
}

TextField(
    value = query,
    onValueChange = { query = it },
    modifier = Modifier
        .focusRequester(fieldRequester)
        .waypointTarget(state, Targets.SearchField),
)
```

## Full example

```kotlin
enum class Targets { Search, Filters, Results }

@Composable
fun SearchScreen() {
    val state = rememberWaypointState {
        step(Targets.Search) { title = "Search bar" }
        step(Targets.Filters) { title = "Filters" }
        step(Targets.Results) { title = "Results" }
    }

    WaypointMaterial3Host(
        state = state,
        keyboardConfig = KeyboardConfig(
            nextKeys = setOf(Key.DirectionRight, Key.Spacebar),
            previousKeys = setOf(Key.DirectionLeft),
            dismissKeys = setOf(Key.Escape),
        ),
        onTourComplete = { analytics.log("tour-completed") },
        onTourCancel = { analytics.log("tour-cancelled") },
    ) {
        SearchContent()
    }
}
```

Press Right or Space to advance, Left to go back, Escape to dismiss.

## See also

- [Accessibility](accessibility.md), how keyboard navigation interacts with focus and screen readers.
- [Interactive Tutorials](interactive-tutorials.md), keyboard setup for hands-on steps.
