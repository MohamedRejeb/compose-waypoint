# Tour Sequences

A `WaypointSequenceState` chains multiple independent tours into one linear flow, auto-advancing from one to the next. Tours don't need persistence for this to work; when a tour has one, the sequence also skips tours the user completed in an earlier session.

Use sequences when a single "onboarding" conceptually spans multiple screens or feature areas and you want each area to remain individually resumable, replayable, and analytics-tagged.

## Core API

Create one sequence from multiple tours you already have:

```kotlin
val welcomeTour = rememberWaypointState<WelcomeKeys>(
    tourId = "welcome",
    persistence = persistence,
) { /* steps */ }

val searchTour = rememberWaypointState<SearchKeys>(
    tourId = "search",
    persistence = persistence,
) { /* steps */ }

val sequence = rememberWaypointSequenceState(welcomeTour, searchTour)

WaypointSequenceEffect(sequence)
```

### Factory

```kotlin
@Composable
public fun rememberWaypointSequenceState(
    vararg tours: WaypointState<*>,
): WaypointSequenceState
```

Returns a state that keeps a reference to each tour. Tours pass through `vararg` order and run in that order.

### Effect

```kotlin
@Composable
public fun WaypointSequenceEffect(state: WaypointSequenceState)
```

Observes the current tour's `isActive`. When a tour transitions to inactive, the effect calls `sequence.advance()` if the tour's `lastEndReason` is `WaypointEndReason.Completed`, otherwise it calls `sequence.stop()`. The decision is based on how the run ended, not on persisted completion, so tours don't need persistence. Without this effect you must call `advance()` manually (for example, from each host's `onTourComplete`).

## State API

| Member | Description |
|--------|-------------|
| `tours: List<WaypointState<*>>` | The tours orchestrated, in order. |
| `activeIndex: Int` | Index of the running tour, `-1` if inactive. |
| `currentTour: WaypointState<*>?` | The running tour, or null. |
| `isActive: Boolean` | True while any tour is running. |
| `isCompleted: Boolean` | True when every tour reports `hasCompleted`. |
| `start()` | Begin from the first incomplete tour. No-op if already active or every tour has been completed. |
| `advance()` | Move to the next incomplete tour. Ends the sequence when none remain. |
| `stop()` | Stop the current tour and halt. |
| `goTo(tour)` | Jump directly to a specific tour if it isn't yet completed. |
| `reset()` | Clear completion for every tour and halt. |

## How each tour keeps its own identity

A sequence does not own the tours. Each `WaypointState` keeps its own:

- Steps and target keys, typed independently (`WelcomeKeys`, `SearchKeys`, and so on).
- Host rendering, place a `WaypointHost` next to each `rememberWaypointState` call.
- `tourId` and `persistence` (optional), which drive `hasCompleted` and cross-session auto-skip.
- `analytics` callbacks, which fire per tour.

The sequence just tracks which tour is active at any moment and coordinates start/advance transitions.

!!! note
    On a configuration change the sequence rebuilds `activeIndex` from the tours' own `isActive` flags. Each tour already survives via `rememberSaveable`, so the sequence itself does not need `rememberSaveable`.

## Auto-skip completed tours

`start()` seeks to the first tour whose `hasCompleted` is false. `advance()` does the same when moving forward. If every tour has been completed, `start()` is a no-op and `isCompleted` is true.

To replay the full sequence, call `reset()`. It clears completion for every tour via their persistence and returns the sequence to an inactive state.

## Completion vs cancellation

`WaypointSequenceEffect` distinguishes the two via `lastEndReason`:

- **Completed**, the user advanced past the last step, `lastEndReason` is `WaypointEndReason.Completed`, the effect calls `advance()`.
- **Cancelled**, the user dismissed the tour before the last step, `lastEndReason` is `WaypointEndReason.Cancelled`, the effect calls `stop()` so the sequence halts rather than skipping to the next tour.

This matches user intent: if a user explicitly dismissed the welcome tour, they probably don't want the search tour to fire right after.

## Full example: three tours on one screen

```kotlin
enum class WelcomeKeys { Header, Cta }
enum class SearchKeys { Bar, Filters }
enum class ProfileKeys { Avatar, Settings }

@Composable
fun HomeScreen() {
    val persistence = rememberSharedPrefsPersistence()

    val welcomeTour = rememberWaypointState<WelcomeKeys>(
        tourId = "welcome",
        persistence = persistence,
    ) {
        step(WelcomeKeys.Header) { title = "Welcome" }
        step(WelcomeKeys.Cta) { title = "Start here" }
    }

    val searchTour = rememberWaypointState<SearchKeys>(
        tourId = "search",
        persistence = persistence,
    ) {
        step(SearchKeys.Bar) { title = "Search" }
        step(SearchKeys.Filters) { title = "Filter results" }
    }

    val profileTour = rememberWaypointState<ProfileKeys>(
        tourId = "profile",
        persistence = persistence,
    ) {
        step(ProfileKeys.Avatar) { title = "Your avatar" }
        step(ProfileKeys.Settings) { title = "Settings live here" }
    }

    val sequence = rememberWaypointSequenceState(
        welcomeTour,
        searchTour,
        profileTour,
    )
    WaypointSequenceEffect(sequence)

    Column {
        Button(onClick = { sequence.start() }) { Text("Start onboarding") }
        Button(onClick = { sequence.reset() }) { Text("Reset") }

        WaypointMaterial3Host(state = welcomeTour) {
            HeaderSection()
        }
        WaypointMaterial3Host(state = searchTour) {
            SearchSection()
        }
        WaypointMaterial3Host(state = profileTour) {
            ProfileSection()
        }
    }
}
```

The three hosts sit side by side. Only one tour is active at a time (the sequence ensures this), so the other hosts stay passive. When the welcome tour completes, the effect calls `advance()` and the search tour starts.

## Multi-screen pattern

When tours live on different screens, place the corresponding `WaypointHost` on each screen and keep the sequence at a level that outlives both. Typically the sequence lives near your navigation root. Because each tour has its own `rememberSaveable` state, tours pause cleanly when their screen leaves the backstack and resume when it returns, and the sequence's `activeIndex` rebuilds from that state on restore.

```kotlin
// AppContainer
val welcomeTour = rememberWaypointState<WelcomeKeys>(tourId = "welcome", persistence = ...) { ... }
val detailsTour = rememberWaypointState<DetailsKeys>(tourId = "details", persistence = ...) { ... }
val sequence = rememberWaypointSequenceState(welcomeTour, detailsTour)
WaypointSequenceEffect(sequence)

NavHost(...) {
    composable("home") {
        WaypointMaterial3Host(state = welcomeTour) { HomeScreen() }
    }
    composable("details") {
        WaypointMaterial3Host(state = detailsTour) { DetailsScreen() }
    }
}
```

Navigate to `"details"` when the welcome tour completes (via `welcomeTour.onTourComplete` or watching `sequence.activeIndex`).

## See also

- [Persistence](persistence.md), how `tourId` + `WaypointPersistence` lets the sequence skip completed tours.
- [WaypointState](../api/waypoint-state.md), the tour state holder a sequence wraps.
