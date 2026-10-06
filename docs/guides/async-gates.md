# Async Gates with `beforeShow`

`beforeShow` is a suspend block attached to a step that runs on every entry to that step, before the step is shown. The highlight and tooltip stay hidden until the block returns. Use it to open a modal, wait for data, let an animation finish, or simply give the UI a moment to settle.

## Core API

Declare the gate in the DSL:

```kotlin
step(Targets.SettingsItem) {
    title = "Settings"
    beforeShow {
        // Open the drawer.
        drawerState.open()
    }
}
```

The property on `WaypointStep` is:

```kotlin
val beforeShow: (suspend () -> Unit)?
```

When the step becomes current, the primary `WaypointHost`:

1. Runs `beforeShow`. While it is suspended the step is hidden: no highlight, no tooltip, and no touch blocking.
2. Shows the step once the block returns and the target is laid out.
3. Starts the step's `advanceOn` block, if it has one.

!!! note
    A gate that returns without suspending never hides a target that is already visible. `beforeShow { showDialog = true }` between two steps inside an open dialog does not make the highlight blink, it animates straight to the new target.

Nothing blocks the screen while a gate is suspended. If the user should not interact with the app in the meantime, check `state.isStepVisible`, see [Block your own UI while a step is pending](interactive-tutorials.md#block-your-own-ui-while-a-step-is-pending).

## Common use cases

### Opening a dialog

```kotlin
var showDialog by remember { mutableStateOf(false) }

step(Targets.DialogButton) {
    title = "Open settings"
}
step(Targets.DialogOption) {
    title = "Configure notifications"
    beforeShow { showDialog = true }
}

if (showDialog) {
    Dialog(onDismissRequest = { showDialog = false }) {
        WaypointMaterial3OverlayHost(state = state) {
            DialogContent() // contains Targets.DialogOption
        }
    }
}
```

The gate opens the dialog, the target inside registers against the overlay host, and the step's highlight and tooltip render inside the dialog as soon as the target is laid out. There is no need to wait for the dialog to mount inside the gate.

### Waiting for data

```kotlin
step(Targets.Leaderboard) {
    title = "Live standings"
    beforeShow {
        viewModel.ensureLeaderboardLoaded()
    }
}
```

If the suspend function can hang, add a timeout:

```kotlin
beforeShow {
    withTimeout(5_000) {
        viewModel.ensureLeaderboardLoaded()
    }
}
```

The user is responsible for adding timeouts, Waypoint does not wrap the lambda.

### Waiting for the UI to settle

```kotlin
step(Targets.ChartLegend) {
    // Wait for a specific animation.
    beforeShow { chartAnimation.animateTo(1f) }
}

step(Targets.SaveButton) {
    // Or just give the previous action a moment.
    beforeShow { delay(300) }
}
```

## Cancellation

The gate runs in a coroutine tied to the current step. When the step changes (the user presses a navigation key, `goToStep()` fires, or the tour is stopped), the coroutine is cancelled and the next step starts with its own gate.

```kotlin
beforeShow {
    // If the user skips ahead, this coroutine is cancelled
    // and never reaches the line below.
    repository.fetchDetails(itemId)
}
```

Respect cancellation inside your lambda: do not catch `CancellationException`, and if you spawn background work, scope it to the lambda rather than a long-lived scope.

## Interaction with cross-hierarchy tours

`beforeShow` pairs naturally with `WaypointOverlayHost` for dialogs, sheets, and popups. The gate opens the modal, the target inside registers against the secondary host, and that host renders the step. Without `beforeShow`, the dialog target would not exist and the step would never appear.

Pattern for dialog-bound steps:

```kotlin
step(Targets.DialogField) {
    title = "Fill this in"
    beforeShow { showDialog = true }
    onExit {
        // Only close if moving forward past the dialog steps.
        if (stepAfterIsOutsideDialog) showDialog = false
    }
}
```

For backwards navigation, use `onEnter` on the non-dialog step to close the dialog:

```kotlin
step(Targets.NextOutsideDialog) {
    onEnter { showDialog = false }
}
```

See [Cross-Hierarchy Tours](multi-host.md) for the full pattern.

## Full example

```kotlin
enum class CheckoutKeys { CartButton, CheckoutDialogSubmit, Confirmation }

@Composable
fun CheckoutScreen() {
    var showCheckoutDialog by remember { mutableStateOf(false) }

    val state = rememberWaypointState {
        step(CheckoutKeys.CartButton) {
            title = "Review your cart"
        }
        step(CheckoutKeys.CheckoutDialogSubmit) {
            title = "Place the order"
            description = "We've filled in a test card for you"
            beforeShow { showCheckoutDialog = true }
        }
        step(CheckoutKeys.Confirmation) {
            title = "Order confirmed"
            beforeShow {
                showCheckoutDialog = false
                viewModel.submitOrder()
            }
        }
    }

    WaypointMaterial3Host(state = state) {
        CartScreenContent()

        if (showCheckoutDialog) {
            Dialog(onDismissRequest = { showCheckoutDialog = false }) {
                WaypointMaterial3OverlayHost(state = state) {
                    CheckoutDialogContent()
                }
            }
        }
    }
}
```

## FAQ

**What if `beforeShow` throws?**
The exception propagates out of the host's effect, like any exception thrown inside a `LaunchedEffect`. Handle errors inside the block, especially if you call into network or IO code that can fail.

**Can I chain `beforeShow` with `advanceOn`?**
Yes, a step can use both. `advanceOn` does not start until the gate has completed and the step is on screen, so a condition that is already satisfied can't skip a step that was never shown. See [Event-Driven Progression](advance-on.md).

**Does `beforeShow` block navigation?**
No. The tooltip is hidden while the gate runs, but keyboard navigation and calls to `state.next()`, `previous()`, `goToStep()` or `stop()` still work. When the step changes, the running `beforeShow` is cancelled and the next step takes over.

## See also

- [Cross-Hierarchy Tours](multi-host.md), the dialog/sheet/popup pattern.
- [WaypointState](../api/waypoint-state.md), the full state surface.
- [Event-Driven Progression](advance-on.md), the post-step counterpart to `beforeShow`.
- [Interactive Tutorials](interactive-tutorials.md), gates in a hands-on tutorial.
