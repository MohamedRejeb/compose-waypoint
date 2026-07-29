# Advance Triggers

`advanceOn` lets a step advance itself when a custom event fires, instead of waiting for the user to click Next. Combine it with a suspend block and the step progresses the moment your condition is met.

Use it for interactive onboarding: advance when the user types in a field, selects an option, scrolls to a position, or completes any observable action.

## Core API

`advanceOn` is a property on `WaypointStep` of type `WaypointTrigger`:

```kotlin
public sealed interface WaypointTrigger {
    public data object NextButton : WaypointTrigger
    public data class Custom(val await: suspend () -> Unit) : WaypointTrigger
    public companion object {
        public val Default: WaypointTrigger = NextButton
    }
}
```

Set it in the step DSL:

```kotlin
step(Targets.SearchField) {
    title = "Try searching"
    advanceOn = WaypointTrigger.Custom {
        snapshotFlow { query.value }
            .filter { it.isNotEmpty() }
            .first()
    }
}
```

When the step becomes active, the host launches a `LaunchedEffect(currentStepIndex)` that waits for the step's `beforeShow` gate to complete (and the tour to be un-paused), then calls `trigger.await()`. A trigger whose condition is already satisfied therefore cannot skip a step that was never shown. When the suspend function returns, the host calls `state.next()`. If the step was last, `onTourComplete` fires.

## How it interacts with NextButton

Setting `advanceOn = Custom(...)` does not remove the Next button or keyboard navigation. The user can still click Next or press their configured next key to advance manually. Custom triggers run in parallel: whichever completes first wins.

!!! tip
    For a target that should advance when clicked, prefer `TargetInteraction.ClickToAdvance` over a custom trigger. It's simpler and automatically handled by `SpotlightOverlay`.

```kotlin
// Prefer this for click-to-advance.
step(Targets.AddButton) {
    title = "Tap to add an item"
    interaction = TargetInteraction.ClickToAdvance
}
```

## Cancellation

The trigger runs inside a `LaunchedEffect` keyed on `currentStepIndex`. When the user navigates manually (Next, Previous, Escape, or the tour stops), the coroutine is cancelled before `await` returns. Waypoint handles this transparently, so your lambda should just propagate cancellation.

Do not catch `CancellationException` inside `advanceOn`:

```kotlin
// Wrong.
advanceOn = WaypointTrigger.Custom {
    try {
        awaitFormSubmission()
    } catch (t: Throwable) {
        // Swallows CancellationException, leaks coroutines.
    }
}
```

## Writing custom triggers

Anything suspending that eventually returns works. The common patterns:

### Flow-based: wait for a state to match

```kotlin
advanceOn = WaypointTrigger.Custom {
    snapshotFlow { viewModel.state.value }
        .filter { it.isFormValid }
        .first()
}
```

### Callback-based: use `suspendCancellableCoroutine`

Wrap a callback that fires once:

```kotlin
advanceOn = WaypointTrigger.Custom {
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
advanceOn = WaypointTrigger.Custom {
    events.filter { it is UserEvent.PhotoUploaded }.first()
}
```

## Full example: advance when user types 3 characters

```kotlin
enum class SearchKeys { SearchField, ResultsArea }

@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf("") }

    val state = rememberWaypointState<SearchKeys> {
        step(SearchKeys.SearchField) {
            title = "Try typing"
            description = "Start a search to continue"
            advanceOn = WaypointTrigger.Custom {
                snapshotFlow { query }
                    .filter { it.length >= 3 }
                    .first()
            }
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

After the user types 3 characters, the step advances automatically to the results step.

## Combining with `beforeShow`

A step can use both:

```kotlin
step(Targets.LoadedPanel) {
    title = "Your dashboard"
    beforeShow { viewModel.loadDashboard() }
    advanceOn = WaypointTrigger.Custom {
        snapshotFlow { viewModel.didUserInteract }
            .filter { it }
            .first()
    }
}
```

`beforeShow` runs first, gating the step visibility. `advanceOn` starts listening only after the gate completes, even if its condition is already met.

## FAQ

**Can I advance to a specific step instead of the next one?**
Call `state.goTo(index)` or `state.goTo(key)` from inside the lambda rather than letting the host call `next()`. But typically the cleaner approach is to use `showIf` to skip intermediate steps instead.

**Does `advanceOn` run on every recomposition?**
No. The `LaunchedEffect` is keyed on `currentStepIndex`, so it only restarts when the step changes.

**What if my `await` never returns?**
The step stays indefinitely until the user navigates manually. Add a `withTimeout` inside the lambda if you need a deadline.

## See also

- [WaypointState](../api/waypoint-state.md), full state and navigation API.
- [Async Gates](async-gates.md), the pre-step counterpart to `advanceOn`.
