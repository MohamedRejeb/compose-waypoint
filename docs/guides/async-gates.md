# Async Gates with `beforeShow`

`beforeShow` is a suspend block attached to a step that runs on every entry to that step. When the step's target isn't laid out yet (for example, it lives in a dialog the gate is about to open), the highlight and tooltip are held back until the block completes. Use it to wait for data, open a modal, run an animation, or perform any asynchronous setup that needs to land before the user sees the spotlight.

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

When the step becomes active, `WaypointHost` launches an effect that:

1. Snaps animated highlight bounds to `Rect.Zero` if the target isn't yet registered (avoids showing the previous step's spotlight on the wrong position).
2. Awaits `beforeShow`.
3. Marks the step as ready, which unblocks the highlight and tooltip.

While `beforeShow` is running, the tooltip and highlight stay hidden only if the target wasn't laid out when the step was entered. A step whose target is already visible shows immediately while the gate runs in the background.

!!! note
    The gate lambda itself runs on every step entry. The hold-back applies only when the step has a `beforeShow` **and** the target isn't already registered. When navigating between two steps inside an already-open modal, `isStepReady` stays true so the highlight animates smoothly to the new position instead of flickering through a hidden frame.

## Common use cases

### Opening a dialog

```kotlin
var showDialog by remember { mutableStateOf(false) }

step(Targets.DialogButton) {
    title = "Open settings"
}
step(Targets.DialogOption) {
    title = "Configure notifications"
    beforeShow {
        showDialog = true
        // Give Compose a frame to mount the dialog and register the target.
        withFrameNanos { }
    }
}

if (showDialog) {
    Dialog(onDismissRequest = { showDialog = false }) {
        WaypointOverlayHost(state = state) {
            DialogContent() // contains Targets.DialogOption
        }
    }
}
```

The dialog mounts only after the previous step, the target inside registers against `WaypointOverlayHost`, and the step's tooltip renders inside the dialog.

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

### Waiting for an animation to settle

```kotlin
step(Targets.ChartLegend) {
    beforeShow {
        chartAnimation.animateTo(1f)
    }
}
```

## Cancellation

The effect is launched as `LaunchedEffect(state.currentStepIndex)`. When the step changes (user clicks Next rapidly, `goTo()` fires, or the tour is stopped), the coroutine is cancelled and `beforeShow` stops awaiting. Waypoint handles this gracefully: on cancellation it does not call `setStepReady(true)`, so the transition to the next step isn't corrupted.

```kotlin
beforeShow {
    // If the user skips ahead, this coroutine is cancelled
    // and never reaches the line below.
    repository.fetchDetails(itemId)
}
```

Respect cancellation inside your lambda: do not catch `CancellationException`, and if you spawn background work, scope it to the lambda rather than a long-lived scope.

## Interaction with cross-hierarchy tours

`beforeShow` pairs naturally with `WaypointOverlayHost` for dialogs, sheets, and popups. The gate opens the modal; the target inside registers against the secondary host; the primary host hands off ownership. Without `beforeShow`, the dialog target wouldn't exist when the step tries to render, and the tooltip would flicker or show on stale bounds.

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
    val state = rememberWaypointState<CheckoutKeys> {
        step(CheckoutKeys.CartButton) {
            title = "Review your cart"
        }
        step(CheckoutKeys.CheckoutDialogSubmit) {
            title = "Place the order"
            description = "We've filled in a test card for you"
            beforeShow {
                showCheckoutDialog = true
                // Wait briefly for the dialog to mount and the target to register.
                withTimeoutOrNull(1_000) {
                    snapshotFlow { state.currentTargetBounds }
                        .first { it != null }
                }
            }
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
            }}
    }
}
```

## FAQ

**What if `beforeShow` throws?**
The exception propagates out of the `LaunchedEffect` block. The step never marks ready, so the tooltip stays hidden. Handle errors explicitly inside the lambda, especially if you call into potentially failing network or IO code.

**Can I chain `beforeShow` with `advanceOn`?**
Yes, a step can use both. A `Custom` trigger doesn't start awaiting until the gate completes and the tour is un-paused, so a pre-satisfied trigger can't skip a step that was never shown. See [Advance Triggers](advance-on.md).

**Does `beforeShow` block navigation?**
No. The user can still hit Next, Previous, or Escape on the host. When they do, the running `beforeShow` is cancelled, and the next step takes over.

## See also

- [Cross-Hierarchy Tours](multi-host.md), the dialog/sheet/popup pattern.
- [WaypointState](../api/waypoint-state.md), the full state surface.
- [Advance Triggers](advance-on.md), the post-step counterpart to `beforeShow`.
