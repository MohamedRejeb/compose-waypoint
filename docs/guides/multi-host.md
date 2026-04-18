# Cross-Hierarchy Tours (Multi-Host)

Dialogs, `ModalBottomSheet`s, and `Popup`s render in a separate composition tree from your screen. A single `WaypointHost` can only reach targets in its own tree, so targets inside a modal are invisible to the host. `WaypointOverlayHost` solves this by letting a secondary host live inside the modal and share state with the primary host.

## The problem

```kotlin
WaypointHost(state = state) {
    MyScreen() // Primary targets register here.

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            // Targets in here are invisible to the outer host.
            DialogContent()
        }
    }
}
```

`Dialog` creates its own `Window` (on Desktop) or its own compose view (on Android). `Modifier.waypointTarget` inside the dialog tries to register, but there's no `WaypointHost` in scope, so the target is dropped.

## The solution

Place a `WaypointOverlayHost` inside the modal, passing the same `WaypointState`:

```kotlin
WaypointHost(state = state) {
    MyScreen()

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            WaypointOverlayHost(state = state) {
                DialogContent() // Targets register here.
            }
        }
    }
}
```

Both hosts share state. Targets register against their nearest host. The overlay and tooltip render in the host that owns the current step's target.

## API

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

It accepts the same styling parameters as `WaypointHost` so each host can use a locally appropriate tooltip or highlight. The Material3 shortcut is `WaypointMaterial3OverlayHost`.

## Architecture

Each host gets a unique `hostId` (`remember { Any() }`) and registers its `LayoutCoordinates` into `state.hostCoordinatesMap`. Targets inside a host resolve their nearest host via `LocalWaypointHostId` and register their bounds in that host's local space.

When the current step's target key resolves to host A, only host A renders the overlay and tooltip. Host B stays passive, its own animated bounds are snapped to `Rect.Zero` so ownership hand-offs don't flash stale positions.

Only the primary host (`WaypointHost`) runs these lifecycle effects:

- `beforeShow` gating.
- Step-transition animated-bounds reset.
- Keyboard handling.
- `onTourComplete` / `onTourCancel` callbacks.

Overlay hosts (`WaypointOverlayHost`) stay silent on lifecycle to avoid duplicating side effects.

## Full example: tour that spans a dialog

```kotlin
enum class TourKeys { HomeButton, DialogField, DialogSubmit, Confirmation }

@Composable
fun CheckoutTour() {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    val state = rememberWaypointState<TourKeys> {
        step(TourKeys.HomeButton) {
            title = "Start checkout"
            description = "Tap here to review your cart"
        }
        step(TourKeys.DialogField) {
            title = "Enter your address"
            beforeShow { showDialog = true }
        }
        step(TourKeys.DialogSubmit) {
            title = "Confirm the order"
        }
        step(TourKeys.Confirmation) {
            title = "Success"
            onEnter { showDialog = false } // Close dialog when moving past it.
        }
    }

    WaypointMaterial3Host(state = state) {
        Column {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.waypointTarget(state, TourKeys.HomeButton),
            ) {
                Text("Open checkout")
            }

            ConfirmationBanner(
                modifier = Modifier.waypointTarget(state, TourKeys.Confirmation),
            )
        }

        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                WaypointMaterial3OverlayHost(state = state) {
                    Column {
                        AddressField(
                            modifier = Modifier.waypointTarget(state, TourKeys.DialogField),
                        )
                        SubmitButton(
                            modifier = Modifier.waypointTarget(state, TourKeys.DialogSubmit),
                        )
                    }
                }
            }
        }
    }
}
```

Flow:

1. Tour starts on `HomeButton`, primary host renders overlay + tooltip.
2. User hits Next. Step moves to `DialogField`. `beforeShow` sets `showDialog = true`.
3. Dialog mounts. Inside it, `WaypointMaterial3OverlayHost` registers. `AddressField` registers against the overlay host.
4. Overlay host takes ownership, primary host goes quiet, overlay host renders the spotlight and tooltip.
5. User advances to `DialogSubmit`, still owned by the overlay host.
6. Next step is `Confirmation` outside the dialog, `onEnter` closes the dialog, overlay host unregisters, primary host takes ownership again.

## Backwards navigation

For tours that can move backwards through the dialog boundary, use `onEnter` on the post-dialog step to re-close the dialog if the user navigates forward past it, and use `beforeShow` on the dialog step to re-open the dialog on the way back:

```kotlin
step(TourKeys.DialogField) {
    // Ensures dialog is open whether we arrive from Home or Confirmation.
    beforeShow { showDialog = true }
}
step(TourKeys.Confirmation) {
    onEnter { showDialog = false }
}
```

`beforeShow` runs on every entry to the step, forward or backward, so the dialog opens consistently.

## Platform support

`WaypointOverlayHost` works wherever Compose Multiplatform's modal primitives do:

- **Android**, `Dialog`, `ModalBottomSheet`, `Popup`.
- **iOS**, Compose `Dialog` and `Popup` (iOS renders modals inside the same window, but the composition is still separate).
- **Desktop**, `Dialog` opens a separate OS window; the overlay host spans just that window.
- **Web (Wasm/JS)**, same composition-tree rules apply.

The shared `WaypointState` is safe across hosts on every platform; there's nothing platform-specific in the host resolution logic.

## `LocalWaypointHostId`

Internally, `LocalWaypointHostId` is a `staticCompositionLocalOf<Any?>` that each host provides. `Modifier.waypointTarget` reads this local to decide which host to register against. This local is `internal` to the core module, so you don't interact with it directly, but it's why placing a `WaypointOverlayHost` inside a modal is sufficient: the targets inside automatically pick up the inner host.

!!! tip
    You can nest overlay hosts further (a popup inside a dialog, for example). Each host scopes its children, and the state layer tracks ownership per target.

## Stacking multiple overlay hosts

A single tour can span any number of hosts. For example, a tour that opens a dialog, then a popup inside that dialog:

```kotlin
WaypointHost(state = state) {
    Screen()

    if (showDialog) Dialog(onDismissRequest = { showDialog = false }) {
        WaypointOverlayHost(state = state) {
            DialogContent()

            if (showPopup) Popup(onDismissRequest = { showPopup = false }) {
                WaypointOverlayHost(state = state) {
                    PopupContent()
                }
            }
        }
    }
}
```

State handles all three hosts transparently.

## See also

- [Async Gates](async-gates.md), `beforeShow` is how you open the modal before the step tries to render.
- [WaypointState](../api/waypoint-state.md), the state holder both hosts share.
