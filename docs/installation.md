# Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.waypoint/waypoint-core)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.waypoint%22)

## Version compatibility

| Kotlin | Compose Multiplatform | Waypoint |
|--------|-----------------------|----------|
| 2.3.20 | 1.10.3                | 0.1.0    |

Waypoint is built with these versions. If you need a release for an older Compose or Kotlin, please open an issue and we will consider publishing one.

## Add the dependency

Add Waypoint to your module `build.gradle.kts`:

=== "Material3 (recommended)"

    ```kotlin
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("com.mohamedrejeb.waypoint:waypoint-material3:{{ waypoint_version }}")
            }
        }
    }
    ```

    `waypoint-material3` exposes `waypoint-core` as an `api` dependency, so you only need this one dependency.

=== "Core only"

    Use `waypoint-core` if you want full control over the tooltip UI, or you don't use Material3.

    ```kotlin
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation("com.mohamedrejeb.waypoint:waypoint-core:{{ waypoint_version }}")
            }
        }
    }
    ```

=== "Version catalog"

    Define Waypoint in `gradle/libs.versions.toml`:

    ```toml
    [versions]
    waypoint = "{{ waypoint_version }}"

    [libraries]
    waypoint-core = { module = "com.mohamedrejeb.waypoint:waypoint-core", version.ref = "waypoint" }
    waypoint-material3 = { module = "com.mohamedrejeb.waypoint:waypoint-material3", version.ref = "waypoint" }
    ```

    Then in your module `build.gradle.kts`:

    ```kotlin
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation(libs.waypoint.material3)  // or libs.waypoint.core
            }
        }
    }
    ```

## Which module do I need?

| You want... | Module |
|---|---|
| Ready-made tooltip with navigation buttons, progress indicator, and Material3 styling | `waypoint-material3` |
| Full control over tooltip appearance (custom composable) | `waypoint-core` |
| Both, Material3 default with per-step overrides | `waypoint-material3` |

!!! tip
    `waypoint-material3` depends on `waypoint-core`, so you never need to declare both.

Both modules expose the Compose artifacts that appear in their public API (`runtime`, `foundation` and `ui` for core, plus `material3` for the Material3 module) as `api` dependencies, at the versions listed above.

## Platform requirements

| Requirement | Minimum |
|---|---|
| Compose Multiplatform | 1.10.3 |
| Kotlin | 2.3.20 |
| Android `minSdk` | 24 |
| iOS deployment target | 14.0 |
| JVM target (Android and Desktop) | 11 |

Published targets: Android, JVM (Desktop), iOS (`iosArm64`, `iosSimulatorArm64`), JS and Wasm (browser). There is no `iosX64` artifact, so the iOS simulator is supported on Apple silicon Macs only.

The iOS minimum is the one Kotlin/Native 2.3 itself targets. Waypoint does not add a requirement of its own.

## iOS framework export

When building a Kotlin Multiplatform framework consumed by iOS, export the Waypoint artifact you use so its public API is visible to Swift:

=== "Regular framework"

    ```kotlin
    kotlin {
        targets
            .filterIsInstance<KotlinNativeTarget>()
            .filter { it.konanTarget.family == Family.IOS }
            .forEach {
                it.binaries.framework {
                    export("com.mohamedrejeb.waypoint:waypoint-material3:{{ waypoint_version }}")
                }
            }
    }
    ```

=== "CocoaPods"

    ```kotlin
    kotlin {
        cocoapods {
            framework {
                export("com.mohamedrejeb.waypoint:waypoint-material3:{{ waypoint_version }}")
            }
        }
    }
    ```

`export` only works for dependencies declared with `api(...)` in the source set. If you only use Waypoint from Kotlin (no Swift call sites), you don't need `export` at all.

## Snapshots

Snapshots are published on every successful build of `main`. They may contain breaking changes, use at your own risk.

Add the snapshots repository:

=== "build.gradle.kts"

    ```kotlin
    allprojects {
        repositories {
            maven("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
    ```

=== "settings.gradle.kts"

    ```kotlin
    dependencyResolutionManagement {
        repositories {
            maven("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
    ```

Then depend on the snapshot version. Snapshots carry the next patch version after the latest release (after `0.1.0` is out they are `0.1.1-SNAPSHOT`), so they always sort after it:

```kotlin
implementation("com.mohamedrejeb.waypoint:waypoint-material3:{{ waypoint_snapshot_version }}")
```

## Next steps

- [Quick Start](getting-started.md), build your first tour
- [Highlight Styles](guides/highlight-styles.md), customize how targets are highlighted
- [API Reference](api/waypoint-state.md), explore the full API
