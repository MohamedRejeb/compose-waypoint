# Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.waypoint/waypoint-core)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.waypoint%22)

## Version compatibility

| Kotlin | Compose Multiplatform | Waypoint |
|--------|-----------------------|----------|
| 2.3.20 | 1.10.3                | 0.1.0    |

If you use an older Compose or Kotlin, please open an issue — we will consider publishing a compatible release.

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

    `waypoint-material3` pulls in `waypoint-core` transitively, so you only need this one dependency.

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
| Both — Material3 default with per-step overrides | `waypoint-material3` |

!!! tip
    `waypoint-material3` depends on `waypoint-core`, so you never need to declare both.

## Platform requirements

| Requirement | Minimum |
|---|---|
| Compose Multiplatform | 1.10+ |
| Kotlin | 2.0+ |
| Android `minSdk` | 24 |
| iOS deployment target | 13+ |
| JVM target | 11 |

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

If you only expose Waypoint to Kotlin (no Swift call sites), `export` is optional.

## Snapshots

Snapshots are published on every successful build of `main`. They may contain breaking changes — use at your own risk.

Add the snapshots repository:

=== "build.gradle.kts"

    ```kotlin
    allprojects {
        repositories {
            maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        }
    }
    ```

=== "settings.gradle.kts"

    ```kotlin
    dependencyResolutionManagement {
        repositories {
            maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
        }
    }
    ```

Then depend on the snapshot version:

```kotlin
implementation("com.mohamedrejeb.waypoint:waypoint-material3:{{ waypoint_version }}-SNAPSHOT")
```

## Next steps

- [Quick Start](getting-started.md) — build your first tour
- [Highlight Styles](guides/highlight-styles.md) — customize how targets are highlighted
- [API Reference](api/waypoint-state.md) — explore the full API
