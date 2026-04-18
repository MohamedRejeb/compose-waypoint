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
) {
    MyScreen()
}
```

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

When `enabled` is true, `WaypointHost` applies a modifier chain equivalent to:

```kotlin
Modifier
    .focusRequester(focusRequester)
    .onPreviewKeyEvent { event -> /* handle next/previous/dismiss */ }
    .focusable()
```

The host requests focus when the tour starts (`LaunchedEffect(state.isActive)`) and clears plus re-requests focus on every step transition (`LaunchedEffect(state.currentStepIndex)`). This ensures:

- Text fields that had focus before the tour lose it, so arrow keys don't scrub through the field instead of stepping through the tour.
- Each step starts with the host holding focus, so keys are captured by Waypoint instead of the previous step's target.

On `KeyDown` events:

- Keys in `nextKeys` call `state.next()`; if that was the last step and the tour ends, `onTourComplete` fires.
- Keys in `previousKeys` call `state.previous()`.
- Keys in `dismissKeys` call `state.stop()` and fire `onTourCancel`.

All other keys fall through, including your app's own shortcuts, so Waypoint doesn't swallow unrelated input.

!!! note
    Because `onPreviewKeyEvent` runs before descendant focus owners see the event, Waypoint catches arrow keys even if a `TextField` was momentarily focused. That's intentional, arrow keys are ambiguous and should belong to the tour while it's running.

## Disabling keyboard

```kotlin
WaypointHost(
    state = state,
    keyboardConfig = KeyboardConfig.Disabled,
) {
    MyScreen()
}
```

When disabled, the host does not attach the key handler or focus requester. The tour still runs, users navigate via tooltip buttons (Next, Back, Skip) or custom triggers.

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

## Focus management during tours

On step transitions, `WaypointHost` calls `focusManager.clearFocus()` before re-requesting focus on itself. If your target is a `TextField`, this means the field loses focus the moment the tour lands on it. Users need to click into the field manually if they want to type, or you can re-request focus yourself inside `onEnter`:

```kotlin
val fieldRequester = remember { FocusRequester() }
step(Targets.SearchField) {
    onEnter {
        // Re-focus the field after the host clears focus.
        fieldRequester.requestFocus()
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
    val state = rememberWaypointState<Targets> {
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
