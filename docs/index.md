# Waypoint

**Product tours and feature showcases for Compose Multiplatform.**

[![Kotlin](https://img.shields.io/badge/kotlin-2.3.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.10.3-blue.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)
[![Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.waypoint/waypoint-core)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.waypoint%22)

Waypoint is a Compose Multiplatform library for building guided product tours, feature discovery flows, and persistent contextual hints. It ships a spotlight overlay, tooltip positioning with auto-flip, pluggable highlight styles, and a state machine for step navigation, all from a simple declarative DSL.

Targets **Android**, **iOS**, **Desktop (JVM)**, and **Web (JS, Wasm)**.

## Artifacts

| Artifact | Description | Platforms | Version |
|----------|-------------|-----------|---------|
| **waypoint-core** | State machine, overlay, tooltip positioning, target registration | Android, iOS, Desktop, Web (JS, Wasm) | [![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.waypoint/waypoint-core)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.waypoint%22%20AND%20a:%22waypoint-core%22) |
| **waypoint-material3** | Material3-styled tooltip with navigation buttons and progress | Android, iOS, Desktop, Web (JS, Wasm) | [![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.waypoint/waypoint-material3)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.waypoint%22%20AND%20a:%22waypoint-material3%22) |

Pick `waypoint-core` alone for full control over the tooltip UI, or `waypoint-material3` for a ready-made Material3 experience. `waypoint-material3` depends on `waypoint-core` transitively.

## Features

<div class="grid cards" markdown>

-   :material-star-circle: __Highlight styles__

    Spotlight, Pulse, Border, Ripple, None, or fully Custom, set per step or host-wide.

-   :material-flare: __Spotlight effects__

    Decorate the cutout with Glow, SoftEdge, or a custom draw lambda.

-   :material-cursor-default-click: __Step navigation__

    `next`, `previous`, `goTo`, `stop`, `pause`, `resume`, fully programmatic.

-   :material-gesture-tap: __Interactive tutorials__

    Let the user type and tap inside the highlighted element while the rest of the screen is blocked.

-   :material-function: __Event-driven progression__

    Advance steps when the user clicks a button, types in a field, or scrolls.

-   :material-card-text-outline: __Intro and outro cards__

    Steps without a target are shown as a centered card.

-   :material-timer-sand: __Async gates__

    Hold a step until a `suspend` block finishes: wait for API responses, animations, or navigation.

-   :material-view-dashboard: __Cross-hierarchy tours__

    Targets inside Dialog, BottomSheet, and Popup via `WaypointOverlayHost`.

-   :material-link-variant: __Tour sequences__

    Chain multiple tours together with auto-advance and optional shared persistence.

-   :material-lightbulb-on: __Persistent hints__

    Ambient beacons on UI elements, independent from tours, with dismiss-and-persist.

-   :material-content-save: __Persistence__

    Remember which tours a user has completed across sessions.

-   :material-keyboard: __Keyboard navigation__

    Arrow keys and Escape on Desktop and Web, configurable per key.

-   :material-chart-line: __Analytics__

    Track tour started, completed, cancelled, and per-step events.

-   :material-theme-light-dark: __Theming__

    Customize colors, typography, dimensions via `WaypointMaterial3Theme`.

</div>

## Quick example

```kotlin
enum class OnboardingTarget { SearchBar, AddButton, Profile }

@Composable
fun HomeScreen() {
    val tourState = rememberWaypointState {
        step(OnboardingTarget.SearchBar) {
            title = "Search"
            description = "Find anything in your workspace."
        }
        step(OnboardingTarget.AddButton) {
            title = "Create"
            description = "Add a new item with one tap."
        }
        step(OnboardingTarget.Profile) {
            title = "Your profile"
            description = "View and edit your account."
        }
    }

    WaypointMaterial3Host(state = tourState) {
        Column {
            SearchBar(Modifier.waypointTarget(tourState, OnboardingTarget.SearchBar))
            FloatingActionButton(
                onClick = {},
                modifier = Modifier.waypointTarget(tourState, OnboardingTarget.AddButton),
            ) { Icon(Icons.Default.Add, "Add") }
            IconButton(
                onClick = {},
                modifier = Modifier.waypointTarget(tourState, OnboardingTarget.Profile),
            ) { Icon(Icons.Default.Person, "Profile") }
        }
    }

    LaunchedEffect(Unit) { tourState.start() }
}
```

## Four entry points

The public API is intentionally minimal, most use cases only need these:

| API | Purpose |
|-----|---------|
| `rememberWaypointState { step(key) { ... } }` | DSL builder that creates a [WaypointState](api/waypoint-state.md) with typed step definitions |
| `Modifier.waypointTarget(state, key)` | Marks a composable as a tour target and registers its bounds |
| `WaypointHost(state) { content }` | Host composable that renders the highlight + tooltip ([docs](api/waypoint-host.md)) |
| `WaypointMaterial3Host(state) { content }` | Same, with Material3-styled tooltip ([docs](api/material3.md)) |

## Next steps

- [Installation](installation.md), add Waypoint to your project
- [Quick Start](getting-started.md), build your first tour step by step
- [Highlight Styles](guides/highlight-styles.md), customize how targets are highlighted
- [Interactive Tutorials](guides/interactive-tutorials.md), hands-on steps the user completes by doing
- [Tour Sequences](guides/tour-sequences.md), chain multiple tours together
- [Persistent Hints](guides/hints.md), ambient beacons outside of tours

## Contribution

Found a bug or want a new feature? [Open an issue](https://github.com/MohamedRejeb/compose-waypoint/issues). Pull requests welcome. :heart:

## Find this library useful? :heart:

Support it by joining [stargazers](https://github.com/MohamedRejeb/compose-waypoint/stargazers). :star: Also, [follow me](https://github.com/MohamedRejeb) on GitHub for more libraries.

## License

```
Copyright 2026 Mohamed Rejeb

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0
```
