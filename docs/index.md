# Waypoint

**Product tours for Compose Multiplatform.**

Waypoint is a library for building guided product tours and feature showcases in Compose Multiplatform applications. It provides a spotlight overlay, tooltip positioning, step-by-step navigation, and multiple highlight modes -- all with a simple declarative API.

## Platforms

Android | iOS | Desktop (JVM) | Web (JS) | Web (Wasm)

## Features

- **Spotlight overlay** -- dimmed background with a transparent cutout around the target element
- **Tooltip positioning** -- auto-placement with flip logic when space is constrained
- **Step navigation** -- next, previous, skip, go-to, pause, and resume
- **Multiple highlight modes** -- Spotlight, Pulse, Border, Ripple, None, or fully Custom
- **Material3 support** -- optional module with ready-to-use styled tooltips
- **Keyboard navigation** -- arrow keys and Escape support out of the box
- **Analytics** -- hook into tour events (start, complete, cancel, step view)
- **Persistence** -- remember which tours a user has completed
- **Conditional steps** -- show or hide steps based on runtime conditions
- **Auto-scroll** -- targets inside scroll containers are scrolled into view automatically
- **Lifecycle callbacks** -- `onEnter` and `onExit` per step

## Quick example

```kotlin
// 1. Define your step keys
enum class OnboardingTarget { SearchBar, AddButton, Profile }

// 2. Create the tour state
val state = rememberWaypointState {
    step(OnboardingTarget.SearchBar) {
        title = "Search"
        description = "Find anything in your workspace."
    }
    step(OnboardingTarget.AddButton) {
        title = "Create"
        description = "Add a new item with one tap."
    }
    step(OnboardingTarget.Profile) {
        title = "Profile"
        description = "View and edit your account."
    }
}

// 3. Wrap your screen and mark targets
WaypointMaterial3Host(state = state) {
    Column {
        SearchBar(
            modifier = Modifier.waypointTarget(state, OnboardingTarget.SearchBar)
        )
        FloatingActionButton(
            onClick = { /* ... */ },
            modifier = Modifier.waypointTarget(state, OnboardingTarget.AddButton)
        ) { Icon(Icons.Default.Add, "Add") }
        IconButton(
            onClick = { /* ... */ },
            modifier = Modifier.waypointTarget(state, OnboardingTarget.Profile)
        ) { Icon(Icons.Default.Person, "Profile") }
    }
}

// 4. Start the tour
LaunchedEffect(Unit) {
    state.start()
}
```

## Modules

| Module | Description | Dependency |
|--------|-------------|------------|
| `waypoint-core` | State machine, overlay, tooltip positioning, target registration | Compose Foundation only |
| `waypoint-material3` | Material3-styled tooltip with navigation buttons and progress | Adds `compose-material3` |

Use `waypoint-core` alone if you want full control over tooltip appearance. Use `waypoint-material3` for a ready-made Material3 experience.

## Next steps

- [Installation](installation.md) -- add Waypoint to your project
- [Quick Start](getting-started.md) -- build your first tour step by step
- [API Reference](api/waypoint-state.md) -- explore the full API
