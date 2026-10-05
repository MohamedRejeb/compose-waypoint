# Event-Driven Progression

`advanceOn` lets a step advance itself when something happens in your app, instead of waiting for the user to click Next. Give it a suspend block, and the step moves on the moment the block returns.

Use it for interactive onboarding: advance when the user types in a field, selects an option, scrolls to a position, or completes any observable action.

## Core API

`advanceOn` is a function in the step DSL that takes a suspend block:

```kotlin
step(Targets.SearchField) {
    title = "Try searching"
    advanceOn { snapshotFlow { query }.first { it.isNotEmpty() } }
}
```

On `WaypointStep` it is stored as:

```kotlin
val advanceOn: (suspend () -> Unit)?
```

When the step becomes current, the host waits until the step is actually on screen (its `beforeShow` gate has completed, its target is laid out, and the tour is not paused), then runs the block. A condition that is already satisfied therefore cannot skip a step the user never saw. When the block returns, the host calls `state.next()`. If the step was the last one, the tour completes and `onTourComplete` fires.

## How it interacts with the Next button

`advanceOn` does not remove the Next button or keyboard navigation. The user can still click Next or press a next key to advance manually, whichever happens first wins. If the step should only advance from the user's action, use a [tooltip without a Next button](interactive-tutorials.md#a-tooltip-without-a-next-button).

## Letting the user reach the target

With the default `Spotlight` highlight the target is blocked like the rest of the screen. A step that waits for the user to do something in the target needs `TargetInteraction.PassThrough`, which lets every gesture inside the highlighted area through:

```kotlin
step(Targets.SearchField) {
    title = "Try searching"
    interaction = TargetInteraction.PassThrough
    advanceOn { snapshotFlow { query }.first { it.isNotEmpty() } }
}
```

`TargetInteraction` has three values:

| Value | Behavior |
|---|---|
| `None` (default) | Touches on the target are swallowed. |
| `ClickToAdvance` | Tapping the target advances the tour. The target itself does not receive the tap. |
| `PassThrough` | All gestures inside the highlighted areas (the target and its `additionalTargets`) reach the app, everything outside stays blocked. |

!!! tip
    If all you need is "tap the target to continue" and the target does not have to react, `ClickToAdvance` is simpler than `PassThrough` plus `advanceOn`.

```kotlin
step(Targets.AddButton) {
    title = "Tap to add an item"
    interaction = TargetInteraction.ClickToAdvance
}
```

Touch blocking only exists for `HighlightStyle.Spotlight`. With any other highlight style the whole screen stays interactive and `interaction` has no effect on touches. See [Interactive Tutorials](interactive-tutorials.md) for the full pattern.

## Cancellation

The block runs in a coroutine tied to the current step. When the user navigates manually (Next, Previous, Escape) or the tour stops, the coroutine is cancelled before the block returns. Waypoint handles this transparently, so your block should just propagate cancellation.

Do not catch `CancellationException` inside `advanceOn`:

```kotlin
// Wrong.
advanceOn {
    try {
        awaitFormSubmission()
    } catch (t: Throwable) {
        // Swallows CancellationException.
    }
}
```

## Writing the block

Anything suspending that eventually returns works. The common patterns:

### Flow-based: wait for a state to match

```kotlin
advanceOn {
    snapshotFlow { viewModel.state.value }.first { it.isFormValid }
}
```

### Callback-based: use `suspendCancellableCoroutine`

Wrap a callback that fires once:

```kotlin
advanceOn {
    suspendCancellableCoroutine { cont ->
        val listener = object : Analytics.EventListener {
            override fun onEvent(name: String) {
                if (name == "photo_uploaded") {
                    cont.resume(Unit)
                }
            }
        }
        analytics.addListener(listener)
        cont.invokeOnCancellation { analytics.removeListener(listener) }
    }
}
```

### Waiting on a shared `MutableSharedFlow`

```kotlin
advanceOn {
    events.first { it is UserEvent.PhotoUploaded }
}
```

## Full example: advance when user types 3 characters

```kotlin
enum class SearchKeys { SearchField, ResultsArea }

@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf("") }

    val state = rememberWaypointState {
        step(SearchKeys.SearchField) {
            title = "Try typing"
            description = "Start a search to continue"
            interaction = TargetInteraction.PassThrough
            advanceOn { snapshotFlow { query }.first { it.length >= 3 } }
        }
        step(SearchKeys.ResultsArea) {
            title = "Results appear here"
            description = "Tap any result to open it"
        }
    }

    WaypointMaterial3Host(state = state) {
        Column {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.waypointTarget(state, SearchKeys.SearchField),
            )
            ResultsList(
                query = query,
                modifier = Modifier.waypointTarget(state, SearchKeys.ResultsArea),
            )
        }
    }
}
```

After the user types 3 characters, the step advances automatically to the results step. The host does not take keyboard focus during a `PassThrough` step, so the user keeps typing in the field without interruption.

## Combining with `beforeShow`

A step can use both:

```kotlin
step(Targets.LoadedPanel) {
    title = "Your dashboard"
    beforeShow { viewModel.loadDashboard() }
    advanceOn { snapshotFlow { viewModel.didUserInteract }.first { it } }
}
```

`beforeShow` runs first and keeps the step hidden until it returns. `advanceOn` starts only after the step is shown, even if its condition is already met.

## FAQ

**Can I advance to a specific step instead of the next one?**
Use `showIf` on the intermediate steps so they are skipped. `advanceOn` always ends with `next()`.

**Does `advanceOn` run on every recomposition?**
No. It is started once per step entry and restarts only when the step changes.

**What if my block never returns?**
The step stays until the user navigates manually. Use `withTimeoutOrNull` inside the block if the step should move on by itself after a deadline.

## See also

- [Interactive Tutorials](interactive-tutorials.md), `advanceOn` with `PassThrough` steps, intro and outro cards.
- [WaypointState](../api/waypoint-state.md), full state and navigation API.
- [Async Gates](async-gates.md), the pre-step counterpart to `advanceOn`.
