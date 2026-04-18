# Waypoint

**Product tours and feature showcases for Compose Multiplatform.**

[![Kotlin](https://img.shields.io/badge/kotlin-2.3.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.10.3-blue.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)
[![MohamedRejeb](https://raw.githubusercontent.com/MohamedRejeb/MohamedRejeb/main/badges/mohamedrejeb.svg)](https://github.com/MohamedRejeb)
[![Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.waypoint/waypoint-core)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.waypoint%22)

Waypoint is a Compose Multiplatform library for building guided product tours, onboarding flows, feature discovery, and persistent contextual hints. It ships a spotlight overlay, tooltip positioning with auto-flip, pluggable highlight styles, and a state machine for step navigation, all from a simple declarative DSL.

Targets **Android**, **iOS**, **Desktop (JVM)**, and **Web (JS, Wasm)**.

## Artifacts

| Artifact | Description | Platforms | Version |
|----------|-------------|-----------|---------|
| **waypoint-core** | State machine, overlay, tooltip positioning, target registration | Android, iOS, Desktop, Web (JS, Wasm) | [![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.waypoint/waypoint-core)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.waypoint%22%20AND%20a:%22waypoint-core%22) |
| **waypoint-material3** | Material3-styled tooltip with navigation buttons and progress | Android, iOS, Desktop, Web (JS, Wasm) | [![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.waypoint/waypoint-material3)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.waypoint%22%20AND%20a:%22waypoint-material3%22) |

Pick `waypoint-core` alone for full control over the tooltip UI, or `waypoint-material3` for a ready-to-use Material3 experience. `waypoint-material3` depends on `waypoint-core` transitively.

## Features

- **Spotlight overlay** with pluggable cutout shapes (Circle, Rect, RoundedRect, Pill)
- **Highlight styles**: Spotlight, Pulse, Border, Ripple, None, or fully Custom, configurable per step
- **Spotlight effects**: Glow, SoftEdge, and custom draw lambdas decorate the cutout
- **Tooltip positioning** with auto-flip and screen-edge clamping
- **Step navigation**: `next`, `previous`, `skip`, `goTo`, `pause`, `resume`
- **Conditional steps** (`showIf`) and lifecycle callbacks (`onEnter`/`onExit`)
- **Event-driven progression** (`advanceOn`) and async gates (`beforeShow`)
- **Multi-element highlight**: one tooltip, multiple targets
- **Cross-hierarchy tours**: targets inside Dialog, Sheet, and Popup via `WaypointOverlayHost`
- **Persistent hints**: ambient beacons that live outside of tours
- **Tour sequences**: chain multiple tours with shared persistence
- **Beacons** (pulse or dot) as standalone attention indicators
- **Auto-scroll** targets into view through nested scroll containers
- **Keyboard navigation** (arrow keys, Escape) on Desktop and Web
- **Analytics**, **persistence**, **theming**, **accessibility** (live-region + RTL) built in
- Survives **configuration changes** via `rememberSaveable`

## Installation

Add the dependency to your module `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Material3 tooltip (recommended)
            implementation("com.mohamedrejeb.waypoint:waypoint-material3:0.1.0")

            // OR: core only, for custom tooltips
            implementation("com.mohamedrejeb.waypoint:waypoint-core:0.1.0")
        }
    }
}
```

See [installation](docs/installation.md) for the version compatibility table and snapshots.

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
            SearchBar(
                modifier = Modifier.waypointTarget(tourState, OnboardingTarget.SearchBar),
            )
            FloatingActionButton(
                onClick = { /* ... */ },
                modifier = Modifier.waypointTarget(tourState, OnboardingTarget.AddButton),
            ) { Icon(Icons.Default.Add, "Add") }
            IconButton(
                onClick = { /* ... */ },
                modifier = Modifier.waypointTarget(tourState, OnboardingTarget.Profile),
            ) { Icon(Icons.Default.Person, "Profile") }
        }
    }

    LaunchedEffect(Unit) { tourState.start() }
}
```

## Documentation

- [Overview](docs/index.md)
- [Installation](docs/installation.md)
- [Quick Start](docs/getting-started.md)
- [Highlight Styles](docs/guides/highlight-styles.md)
- [Custom Tooltips](docs/guides/custom-tooltips.md)
- [Tour Sequences](docs/guides/tour-sequences.md)
- [Persistent Hints](docs/guides/hints.md)
- [Theming](docs/guides/theming.md)
- [Analytics](docs/guides/analytics.md)
- [Persistence](docs/guides/persistence.md)
- [API Reference](docs/api/waypoint-state.md)

## Sample

The `:sample` module contains demos for every feature, runnable on all targets:

```bash
# Desktop
./gradlew :sample:run

# Android
./gradlew :sample:assembleDebug

# Web (Wasm)
./gradlew :sample:wasmJsBrowserDevelopmentRun

# Web (JS)
./gradlew :sample:jsBrowserDevelopmentRun
```

For iOS, open `iosApp/` in Xcode and run.

## Contribution

If you've found a bug or want a new feature, please [open an issue](https://github.com/MohamedRejeb/compose-waypoint/issues). Pull requests are welcome. :heart:

## Find this library useful? :heart:

Support it by joining [stargazers](https://github.com/MohamedRejeb/compose-waypoint/stargazers) for this repository. :star:
Also, [follow me](https://github.com/MohamedRejeb) on GitHub for more libraries.

## License

```
Copyright 2026 Mohamed Rejeb

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0
```
