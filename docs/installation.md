# Installation

## Version catalog setup

Add Waypoint to your Gradle version catalog (`gradle/libs.versions.toml`):

```toml
[versions]
waypoint = "{{ waypoint_version }}"

[libraries]
waypoint-core = { module = "com.mohamedrejeb.waypoint:waypoint-core", version.ref = "waypoint" }
waypoint-material3 = { module = "com.mohamedrejeb.waypoint:waypoint-material3", version.ref = "waypoint" }
```

## Add the dependency

=== "Material3 (recommended)"

    If your app uses Material3, depend on `waypoint-material3`. It includes `waypoint-core` transitively.

    ```kotlin
    // build.gradle.kts
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation(libs.waypoint.material3)
            }
        }
    }
    ```

=== "Core only"

    If you do not use Material3, or you want full control over tooltip UI, depend on `waypoint-core` alone.

    ```kotlin
    // build.gradle.kts
    kotlin {
        sourceSets {
            commonMain.dependencies {
                implementation(libs.waypoint.core)
            }
        }
    }
    ```

## Which module do I need?

| You want... | Module |
|---|---|
| Ready-made tooltip with navigation buttons, progress indicator, and Material3 styling | `waypoint-material3` |
| Full control over tooltip appearance (custom composable) | `waypoint-core` |
| Both -- Material3 default with per-step overrides | `waypoint-material3` |

!!! tip
    `waypoint-material3` depends on `waypoint-core`, so you never need to declare both.

## Platform requirements

| Requirement | Minimum |
|---|---|
| Compose Multiplatform | 1.10+ |
| Kotlin | 2.0+ |
| Android `minSdk` | 24 |
| JVM target | 11 |
