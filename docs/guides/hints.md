# Hints

Hints are persistent, independent beacons attached to UI elements. Unlike tour steps, hints do not progress sequentially; each one stays visible until the user dismisses it, and dismissal can be persisted across sessions.

Use hints to draw attention to a new feature on a screen the user has already seen, flag an optional setting, or nudge users toward features they haven't discovered.

## Core API

Declare hints with a DSL, then wrap your content with `WaypointHint`:

```kotlin
enum class HomeHints { NewFilter, Notifications }

val hints = rememberWaypointHintState<HomeHints>(
    persistence = persistence,
    groupId = "home",
) {
    hint(HomeHints.NewFilter) {
        title = "New filter"
        description = "Narrow results by date range"
        placement = TooltipPlacement.Bottom
    }
    hint(HomeHints.Notifications) {
        title = "Notifications"
        description = "Tap here to manage alerts"
        beaconAlignment = Alignment.TopStart
    }
}

WaypointHint(
    state = hints,
    key = HomeHints.NewFilter,
    tooltipContent = { scope ->
        MyHintCard(
            title = scope.title,
            description = scope.description,
            onGotIt = { scope.dismiss() },
        )
    },
) {
    FilterButton()
}
```

### Factory

```kotlin
@Composable
public fun <K> rememberWaypointHintState(
    persistence: WaypointPersistence? = null,
    groupId: String? = null,
    builder: WaypointHintScope<K>.() -> Unit,
): WaypointHintState<K>
```

`persistence` needs to be non-null for dismissal to survive across sessions; `groupId` is an optional namespace. Dismissed keys and the open hint are restored on configuration change via `rememberSaveable`.

### Hint properties

`WaypointHint` is a data class with these fields, each configurable in the DSL:

| Property | Default | Description |
|----------|---------|-------------|
| `key: K` | required | Unique identifier, typically an enum case. |
| `title: String?` | `null` | Tooltip title. |
| `description: String?` | `null` | Tooltip description. |
| `placement: TooltipPlacement` | `Auto` | Where the tooltip appears relative to the target. |
| `beaconStyle: BeaconStyle` | `Pulse()` | Visual style for the beacon indicator. |
| `beaconAlignment: Alignment` | `TopEnd` | Where the beacon sits on the content. |
| `beaconOffset: DpOffset` | `Zero` | Extra offset applied after alignment. |

### Wrapping content

```kotlin
@Composable
public fun <K> WaypointHint(
    state: WaypointHintState<K>,
    key: K,
    modifier: Modifier = Modifier,
    tooltipSpacing: Dp = WaypointDefaults.TooltipSpacing,
    screenMargin: Dp = WaypointDefaults.ScreenMargin,
    tooltipContent: @Composable (HintScope) -> Unit,
    content: @Composable () -> Unit,
)
```

If `key` is not registered, `WaypointHint` renders `content` plainly without any hint decoration. Same if the hint has been dismissed.

## Material3 shortcut

For a ready-made tooltip, use `WaypointMaterial3Hint`:

```kotlin
WaypointMaterial3Hint(
    state = hints,
    key = HomeHints.NewFilter,
    labels = WaypointMaterial3Labels(gotIt = "Got it", close = "Close"),
    showCloseButton = true,
) {
    FilterButton()
}
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `labels` | `WaypointMaterial3Labels.Default` | Texts: `gotIt` for the primary dismiss button, `close` as the close icon's content description. |
| `showCloseButton` | `false` | Render an extra close-only button that calls `scope.close()`. |

The tooltip draws colors, typography, and dimensions from `WaypointMaterial3Theme`.

## HintScope

Tooltip content composables receive a `HintScope`:

```kotlin
public interface HintScope {
    public val title: String?
    public val description: String?
    public val placement: ResolvedPlacement
    public fun dismiss()
    public fun close()
}
```

- **`placement`** is the side of the target the tooltip ended up on, after auto-flip.
- **`dismiss()`** marks the hint permanently dismissed, persists the state if configured, and hides the beacon. Use for "Got it" actions.
- **`close()`** hides the tooltip but leaves the beacon visible. Use for "X" close buttons when you want the user to be able to reopen the tooltip later.

To give a custom hint tooltip an arrow, wrap it in `TooltipArrowBox`, exactly as for tour tooltips. See [Arrows](custom-tooltips.md#arrows).

## Persistence

When `persistence` is provided, the state writes dismissals to `"<groupId>:<key>"`, or to `"hint:<key>"` when `groupId` is null (the prefix keeps hint keys from colliding with tour ids). On init, each hint's persistence key is checked, hints whose key reports completed are marked dismissed.

```kotlin
val hints = rememberWaypointHintState<HomeHints>(
    persistence = sharedPrefsPersistence,
    groupId = "home",
) { /* ... */ }
```

This keeps hint groups namespaced, an enum named `NewFilter` can live in both `"home"` and `"settings"` groups without colliding. See [Persistence](persistence.md) for implementing `WaypointPersistence`.

## State API

| Member | Description |
|--------|-------------|
| `hints: List<WaypointHint<K>>` | All declared hints. |
| `openHintKey: K?` | The currently open tooltip's key, or null. Only one can be open at a time. |
| `isDismissed(key)` | True if that hint has been dismissed. |
| `find(key)` | Look up a hint by key. |
| `open(key)` | Open the tooltip for a hint. No-op if the hint is dismissed. |
| `close()` | Close any open tooltip without dismissing. |
| `dismiss(key)` | Permanently dismiss and persist if configured. |
| `reset(key)` | Un-dismiss a single hint so its beacon shows again. |
| `resetAll()` | Un-dismiss every hint. |

## Full example

```kotlin
enum class HomeHints { Filter, Sort, Share }

@Composable
fun HomeScreen() {
    val persistence = rememberSharedPrefsPersistence()
    val hints = rememberWaypointHintState<HomeHints>(
        persistence = persistence,
        groupId = "home",
    ) {
        hint(HomeHints.Filter) {
            title = "Filter results"
            description = "Narrow down by category, date, or tag"
        }
        hint(HomeHints.Sort) {
            title = "Sort order"
            description = "Switch between newest, popular, and nearest"
            placement = TooltipPlacement.Start
        }
        hint(HomeHints.Share) {
            title = "Share"
            description = "Copy a link or send to a contact"
            beaconStyle = BeaconStyle.Dot(color = Color(0xFF3F51B5))
            beaconAlignment = Alignment.BottomEnd
        }
    }

    Column {
        Row {
            WaypointMaterial3Hint(state = hints, key = HomeHints.Filter) {
                IconButton(onClick = { /* filter */ }) { Icon(Icons.Default.FilterList, null) }
            }
            WaypointMaterial3Hint(state = hints, key = HomeHints.Sort) {
                IconButton(onClick = { /* sort */ }) { Icon(Icons.Default.Sort, null) }
            }
            WaypointMaterial3Hint(state = hints, key = HomeHints.Share) {
                IconButton(onClick = { /* share */ }) { Icon(Icons.Default.Share, null) }
            }
        }

        TextButton(onClick = { hints.resetAll() }) {
            Text("Show hints again")
        }
    }
}
```

## Hints vs tours

| Concern | Hints | Tours |
|---------|-------|-------|
| Progression | Independent, each hint stands alone | Sequential, one step at a time |
| User control | User chooses which beacon to tap | Linear flow, next/previous/skip |
| Visual | Small beacon on the target | Full-screen overlay + highlight |
| Persistence | Per-hint dismissal | Per-tour completion |
| Use case | Draw attention to individual new features | Walk users through a flow on first visit |

Combine them, run a tour for the initial onboarding, then leave hints on newer features so returning users can self-discover.

## See also

- [Beacons](beacons.md), the low-level indicator primitive hints are built on.
- [Persistence](persistence.md), plug a `WaypointPersistence` into hint state.
- [Custom Tooltips](custom-tooltips.md), the same tooltip composition patterns apply to `HintScope`.
