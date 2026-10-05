# WaypointState

`WaypointState<K>` is the central state holder for a tour. It tracks the current step, exposes navigation methods, coordinates target registration, and wires up analytics and persistence.

Create a `WaypointState` with [`rememberWaypointState`](#factory-rememberwaypointstate), pass it to `WaypointHost` (or `WaypointMaterial3Host`), and call `start()` when you're ready to run the tour.

## Class signature

```kotlin
@Stable
public class WaypointState<K>(
    public val steps: List<WaypointStep<K>>,
    public val tourId: String? = null,
    public val analytics: WaypointAnalytics? = null,
    public val persistence: WaypointPersistence? = null,
)
```

You usually don't construct `WaypointState` directly, use `rememberWaypointState`, which handles config-change survival via `rememberSaveable`.

| Constructor parameter | Type | Purpose |
|---|---|---|
| `steps` | `List<WaypointStep<K>>` | The ordered list of steps in the tour. |
| `tourId` | `String?` | Optional identifier used by `analytics` and `persistence`. |
| `analytics` | `WaypointAnalytics?` | Optional event listener. See [Analytics](../guides/analytics.md). |
| `persistence` | `WaypointPersistence?` | Optional completion tracker. See [Persistence](../guides/persistence.md). |

## Public properties

| Property | Type | Description |
|---|---|---|
| `steps` | `List<WaypointStep<K>>` | Immutable list of steps. |
| `tourId` | `String?` | Identifier passed at construction. |
| `analytics` | `WaypointAnalytics?` | Analytics listener passed at construction. |
| `persistence` | `WaypointPersistence?` | Persistence listener passed at construction. |
| `currentStepIndex` | `Int` | Raw index of the current step in the full `steps` list (hidden steps included), or `-1` when inactive. Backed by `mutableStateOf`. |
| `isActive` | `Boolean` | True while the tour is showing steps. |
| `isPaused` | `Boolean` | True if the tour is paused. |
| `currentStep` | `WaypointStep<K>?` | The current step, or null when inactive. |
| `isStepVisible` | `Boolean` | True while the current step is on screen: the tour is active and not paused, the step's `beforeShow` gate has completed, and its target is laid out (or it has no target). Use it to block your own UI while a step is pending. |
| `currentTargetBounds` | `Rect?` | Bounds of the current step's target in the coordinate space of the host that owns it. Null when the tour is inactive, the target isn't laid out or is scrolled out of view, or the step has no target. |
| `lastEndReason` | `WaypointEndReason?` | How the most recent run ended (`Completed` or `Cancelled`), or null until a run ends. Updated on every completion or cancellation, including direct `stop()` calls. |
| `hasCompleted` | `Boolean` | True if `tourId` and `persistence` are set and `persistence.isCompleted(tourId)` returns true. |

All reactive properties are backed by Compose state, so reading them in a composable triggers recomposition, and they can be observed with `snapshotFlow`.

## Public methods

### `start()`

Starts the tour from the first visible step. No-op if:

- The tour is already active.
- `hasCompleted` is true.
- No step passes its `showIf` predicate.

Fires `WaypointAnalytics.onTourStarted` when it does start.

```kotlin
LaunchedEffect(Unit) { tourState.start() }
```

### `next()`

Advances to the next visible step. If already on the last step, completes the tour (marks persistence, fires `onTourCompleted`). No-op when inactive or paused.

### `previous()`

Goes back to the previous visible step. No-op when inactive, paused, or already on the first visible step.

### `goTo(index: Int)`

Jumps to a specific step by index. Ignores the jump when:

- Inactive or paused.
- Index is out of range, or already the current step.
- The step at that index has a `showIf` that returns false.

### `goTo(key: K)`

Jumps to a step by its target key. Looks up the first step whose `targetKey == key` and delegates to `goTo(index)`. Steps without a target can only be reached by index.

### `stop()`

Cancels the tour, fires the current step's `onExit`, sets `lastEndReason` to `Cancelled`, and emits `WaypointAnalytics.onTourCancelled`. The host's `onTourCancel` callback fires too, even for direct `stop()` calls from app code. No-op if already inactive and not paused.

### `pause()`

Hides the highlight / tooltip but preserves state. No-op unless the tour is active and not already paused.

### `resume()`

Un-pauses. No-op unless paused.

### `markCompleted()`

Force-marks the tour as completed in persistence. No-op when `tourId` or `persistence` is null. Useful for "don't show again" buttons.

### `resetCompletion()`

Removes the completion record from persistence so `start()` will run the tour again. No-op when `tourId` or `persistence` is null.

### Invariants

- `next()`, `previous()`, and both `goTo(...)` methods no-op while `!isActive || isPaused`.
- `start()` no-ops when `isActive` is already true.
- `stop()` transitions through the current step's `onExit` before resetting.
- Completion via `next()` on the last step marks persistence; cancellation (`stop()`, `skip`) does **not**.

## Factory: `rememberWaypointState`

Two overloads exist. Both survive configuration changes (Android rotation, theme flip) via `rememberSaveable`. Only the primitive tour state (step index, active, paused) is saved, target coordinates and lambdas are re-registered after recomposition.

### DSL overload

```kotlin
@Composable
public fun <K> rememberWaypointState(
    tourId: String? = null,
    analytics: WaypointAnalytics? = null,
    persistence: WaypointPersistence? = null,
    builder: WaypointStepBuilder<K>.() -> Unit,
): WaypointState<K>
```

```kotlin
val tourState = rememberWaypointState(
    tourId = "onboarding",
    analytics = MyAnalytics,
    persistence = MyPersistence,
) {
    step(Targets.Search) {
        title = "Search"
        description = "Find anything in your workspace."
    }
    step(Targets.Profile) {
        title = "Your profile"
        description = "Manage your account."
    }
}
```

### Pre-built list overload

Use this when you build the list of `WaypointStep<K>` elsewhere (for example, from a remote config).

```kotlin
@Composable
public fun <K> rememberWaypointState(
    steps: List<WaypointStep<K>>,
    tourId: String? = null,
    analytics: WaypointAnalytics? = null,
    persistence: WaypointPersistence? = null,
): WaypointState<K>
```

## DSL: `step`

Inside the `rememberWaypointState { ... }` block, each `step` call declares one step. Steps run in declaration order, subject to `showIf`.

| Function | Purpose |
|---|---|
| `step(targetKey) { ... }` | A step that highlights the composable marked with `Modifier.waypointTarget(state, targetKey)`. The block is optional. |
| `step { ... }` | A step without a target. Its tooltip is shown centered over the primary `WaypointHost`, which suits intro and outro cards. |

```kotlin
step(Targets.Search) {
    title = "Search"
    description = "Find anything fast."
    placement = TooltipPlacement.Bottom
    highlightStyle = HighlightStyle.Spotlight(shape = SpotlightShape.Pill)
    interaction = TargetInteraction.PassThrough
    additionalTargets = listOf(Targets.SearchHint)
    showIf { featureEnabled() }
    onEnter { analytics.log("search_step_viewed") }
    onExit { }
    beforeShow { viewModel.openSearchPanel() }
    advanceOn { snapshotFlow { searchQuery }.first { it.isNotEmpty() } }
    content { stepScope -> MyCustomTooltip(stepScope) }
}

step {
    title = "You're all set"
    description = "Search is always one tap away."
}
```

### StepBuilder properties

| Property | Type | Default | Purpose |
|---|---|---|---|
| `title` | `String?` | `null` | Tooltip title, exposed to tooltips as `StepScope.title`. |
| `description` | `String?` | `null` | Tooltip description, exposed as `StepScope.description`. |
| `placement` | `TooltipPlacement` | `Auto` | Desired side. Waypoint auto-flips if space is tight. Ignored without a target. |
| `highlightStyle` | `HighlightStyle?` | `null` (inherits host) | Per-step highlight. See [Highlight Styles](../guides/highlight-styles.md). |
| `interaction` | `TargetInteraction` | `None` | What touches on the highlighted target do. Ignored without a target. |
| `additionalTargets` | `List<K>` | `emptyList()` | Extra keys to highlight alongside the primary target. Must live in the same host as the primary target; keys registered against a different host are ignored for that step. Ignored without a target. |

### StepBuilder methods

| Method | Purpose |
|---|---|
| `showIf { condition() }` | Skip this step when the predicate returns `false`. Called when navigating through the tour. |
| `onEnter { }` | Callback when the step becomes active. |
| `onExit { }` | Callback when the step is exited (advance, back, stop). |
| `beforeShow { suspendWork() }` | Suspend block that runs on every step entry. The step is not shown until it returns. See [Async Gates](../guides/async-gates.md). |
| `advanceOn { awaitSomething() }` | Suspend block started once the step is shown. The tour advances when it returns. See [Event-Driven Progression](../guides/advance-on.md). |
| `content { stepScope -> ... }` | Per-step custom tooltip. Bypasses the host-level `tooltipContent`. See [Custom Tooltips](../guides/custom-tooltips.md). |

`beforeShow` always gates: the highlight and tooltip stay hidden until the block returns, so it can open a dialog that contains the target, wait for data, or just `delay(300)` to let the UI settle. A block that returns without suspending never hides a target that is already visible, which keeps navigation between such steps flicker-free.

`advanceOn` starts once the step is on screen (gate completed, target laid out, tour not paused), so a condition that is already satisfied cannot skip a step that was never shown. The Next button and keyboard shortcuts still work alongside it.

### `TargetInteraction`

| Value | Behavior |
|---|---|
| `None` | Touches on the target are swallowed. |
| `ClickToAdvance` | Tapping the target advances the tour. The target itself does not receive the tap. |
| `PassThrough` | All gestures inside the highlighted areas (the target and every additional target) reach the app, everything outside stays blocked. While such a step is shown the host does not take keyboard focus and only handles the dismiss keys, so the user can type in the target. |

Touch blocking only exists for `HighlightStyle.Spotlight`. Every other highlight style leaves the whole screen interactive. For blocking without dimming use `HighlightStyle.Spotlight(overlayAlpha = 0f)`. See [Interactive Tutorials](../guides/interactive-tutorials.md).

## `WaypointStep`

The DSL builds a list of `WaypointStep<K>`. Construct them directly when using the pre-built list overload:

```kotlin
@Immutable
public data class WaypointStep<K>(
    val targetKey: K? = null,
    val title: String? = null,
    val description: String? = null,
    val content: (@Composable (StepScope) -> Unit)? = null,
    val placement: TooltipPlacement = TooltipPlacement.Auto,
    val highlightStyle: HighlightStyle? = null,
    val interaction: TargetInteraction = TargetInteraction.None,
    val advanceOn: (suspend () -> Unit)? = null,
    val additionalTargets: List<K> = emptyList(),
    val showIf: (() -> Boolean)? = null,
    val onEnter: (() -> Unit)? = null,
    val onExit: (() -> Unit)? = null,
    val beforeShow: (suspend () -> Unit)? = null,
)
```

A `targetKey` of `null` makes a step without a target.

## Minimal end-to-end example

```kotlin
enum class Targets { Search, Add, Profile }

@Composable
fun TourScreen() {
    val tourState = rememberWaypointState(tourId = "home_onboarding") {
        step(Targets.Search) {
            title = "Search"
            description = "Find anything in your workspace."
        }
        step(Targets.Add) {
            title = "Create"
            description = "Start something new."
        }
        step(Targets.Profile) {
            title = "Profile"
            description = "Manage your account."
        }
    }

    LaunchedEffect(Unit) { tourState.start() }

    WaypointMaterial3Host(state = tourState) {
        MyScreen(tourState)
    }
}
```

## See also

- [WaypointHost API](waypoint-host.md), the host that renders the tour
- [Material3 API](material3.md), convenience wrappers with pre-styled tooltips
- [Interactive Tutorials](../guides/interactive-tutorials.md), hands-on steps with `PassThrough` and `advanceOn`
- [Highlight Styles](../guides/highlight-styles.md), visual emphasis per step or host-wide
- [Custom Tooltips](../guides/custom-tooltips.md), replace the tooltip body
- [Analytics](../guides/analytics.md), track tour engagement
- [Persistence](../guides/persistence.md), remember completed tours
