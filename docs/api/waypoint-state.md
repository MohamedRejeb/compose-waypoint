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
| `currentTargetBounds` | `Rect?` | Bounds of the current step's target, or null if the target hasn't registered. |
| `lastEndReason` | `WaypointEndReason?` | How the most recent run ended (`Completed` or `Cancelled`), or null until a run ends. Updated on every completion or cancellation, including direct `stop()` calls. |
| `hasCompleted` | `Boolean` | True if `tourId` and `persistence` are set and `persistence.isCompleted(tourId)` returns true. |

All reactive properties are backed by Compose state, so reading them in a composable triggers recomposition.

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
- Index is out of range.
- The step at that index has a `showIf` that returns false.

### `goTo(key: K)`

Jumps to a step by its target key. Looks up the first step whose `targetKey == key` and delegates to `goTo(index)`.

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

## DSL: `step(targetKey) { ... }`

Inside the `rememberWaypointState { ... }` block, `step(key) { ... }` declares one step. Steps run in declaration order, subject to `showIf`.

```kotlin
step(Targets.Search) {
    title = "Search"
    description = "Find anything fast."
    placement = TooltipPlacement.Bottom
    highlightStyle = HighlightStyle.Pulse(color = MaterialTheme.colorScheme.primary)
    interaction = TargetInteraction.AllowClick
    showIf { featureEnabled() }
    onEnter { analytics.log("search_step_viewed") }
    onExit { }
    beforeShow { viewModel.openSearchPanel() }
    advanceOn = WaypointTrigger.Custom {
        snapshotFlow { searchQuery.value }.filter { it.isNotEmpty() }.first()
    }
    additionalTargets = listOf(Targets.SearchHint)
    content { stepScope -> MyCustomTooltip(stepScope) }
}
```

### StepBuilder properties

| Property | Type | Default | Purpose |
|---|---|---|---|
| `title` | `String?` | `null` | Tooltip title (used by the Material3 tooltip). |
| `description` | `String?` | `null` | Tooltip description. |
| `content` | `(@Composable (StepScope) -> Unit)?` | `null` | Per-step custom tooltip. Bypasses the host-level `tooltipContent`. See [Custom Tooltips](../guides/custom-tooltips.md). |
| `placement` | `TooltipPlacement` | `Auto` | Desired side. Waypoint auto-flips if space is tight. |
| `highlightStyle` | `HighlightStyle?` | `null` (inherits host) | Per-step highlight. See [Highlight Styles](../guides/highlight-styles.md). |
| `interaction` | `TargetInteraction` | `None` | Whether the user can tap the highlighted target. |
| `advanceOn` | `WaypointTrigger` | `NextButton` | How the step progresses. Use `WaypointTrigger.Custom` for async gates. |
| `additionalTargets` | `List<K>` | `emptyList()` | Extra keys to highlight alongside the primary target. Must live in the same host as the primary target; keys registered against a different host are ignored for that step. |

### StepBuilder methods

| Method | Purpose |
|---|---|
| `showIf { condition() }` | Skip this step when the predicate returns `false`. Called when navigating through the tour. |
| `onEnter { }` | Callback when the step becomes active. |
| `onExit { }` | Callback when the step is exited (advance, back, stop). |
| `beforeShow { suspendWork() }` | Suspend block that runs on every step entry. Use for opening modals, waiting for navigation, async prefetching. |
| `content { stepScope -> ... }` | Alternative to the property form, sets `content` via a method call. |

`beforeShow` is useful when the target mounts inside a `Dialog` or a scroll list, open the dialog, wait for composition, then let Waypoint render. The highlight and tooltip are held back until the gate completes only when the step's target isn't laid out yet (the dialog/sheet case); if the target is already visible, the step shows immediately while `beforeShow` runs.

`advanceOn = WaypointTrigger.Custom { ... }` starts the suspend lambda once the `beforeShow` gate completes and the tour is un-paused, so a pre-satisfied trigger cannot skip a step that was never shown. When it returns, the step advances automatically. The Next button and keyboard shortcuts still work alongside it.

## Minimal end-to-end example

```kotlin
enum class Targets { Search, Add, Profile }

@Composable
fun TourScreen() {
    val tourState = rememberWaypointState<Targets>(tourId = "home_onboarding") {
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
- [Highlight Styles](../guides/highlight-styles.md), visual emphasis per step or host-wide
- [Custom Tooltips](../guides/custom-tooltips.md), replace the tooltip body
- [Analytics](../guides/analytics.md), track tour engagement
- [Persistence](../guides/persistence.md), remember completed tours
