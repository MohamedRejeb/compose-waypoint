# Sample App Rework — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the `composeApp` module with a polished, catalog-style `sample` app that demonstrates every Waypoint library feature across 8 realistic demos with responsive Material3 adaptive layout, Navigation3, and ViewModel+MVI.

**Architecture:** Catalog home screen → 8 independent demo screens. Navigation3 handles routing with predictive back. Material3 adaptive provides responsive nav (bottom bar/rail/drawer). ViewModel+MVI for 4 complex demos. Custom theme with Plus Jakarta Sans font and violet/teal palette.

**Tech Stack:** Kotlin 2.3.20, Compose Multiplatform 1.10.3, Navigation3, Material3 Adaptive, kotlinx-serialization, ViewModel

**Spec:** `docs/superpowers/specs/2026-04-15-sample-app-rework-design.md`

---

## File Structure

All new files live under `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/`. Platform entry points are minimal (one file each).

```
sample/
├── build.gradle.kts
├── src/commonMain/
│   ├── kotlin/com/mohamedrejeb/waypoint/sample/
│   │   ├── App.kt
│   │   ├── theme/
│   │   │   ├── Color.kt
│   │   │   ├── Type.kt
│   │   │   └── Theme.kt
│   │   ├── navigation/
│   │   │   └── Route.kt
│   │   ├── catalog/
│   │   │   ├── CatalogScreen.kt
│   │   │   └── DemoCard.kt
│   │   ├── components/
│   │   │   └── DemoScaffold.kt
│   │   └── demos/
│   │       ├── onboarding/OnboardingDemo.kt
│   │       ├── discovery/
│   │       │   ├── DiscoveryContract.kt
│   │       │   ├── DiscoveryViewModel.kt
│   │       │   └── FeatureDiscoveryDemo.kt
│   │       ├── tutorial/
│   │       │   ├── TutorialContract.kt
│   │       │   ├── TutorialViewModel.kt
│   │       │   └── InteractiveTutorialDemo.kt
│   │       ├── multitarget/MultiTargetDemo.kt
│   │       ├── highlights/HighlightGalleryDemo.kt
│   │       ├── theming/
│   │       │   ├── ThemingContract.kt
│   │       │   ├── ThemingViewModel.kt
│   │       │   └── ThemingPlaygroundDemo.kt
│   │       ├── analytics/
│   │       │   ├── AnalyticsContract.kt
│   │       │   ├── AnalyticsViewModel.kt
│   │       │   └── AnalyticsDashboardDemo.kt
│   │       └── modals/ModalToursDemo.kt
│   └── composeResources/font/
│       ├── PlusJakartaSans-Regular.ttf    (already exists)
│       ├── PlusJakartaSans-Medium.ttf     (already exists)
│       ├── PlusJakartaSans-SemiBold.ttf   (already exists)
│       └── PlusJakartaSans-Bold.ttf       (already exists)
├── src/androidMain/kotlin/.../MainActivity.kt
├── src/iosMain/kotlin/.../MainViewController.kt
├── src/jvmMain/kotlin/.../main.kt
├── src/jsMain/kotlin/.../main.js.kt
└── src/wasmJsMain/kotlin/.../main.wasmJs.kt
```

---

## Task 1: Module Setup — Build Config & Platform Entry Points

**Files:**
- Modify: `settings.gradle.kts`
- Modify: `gradle/libs.versions.toml`
- Create: `sample/build.gradle.kts`
- Create: `sample/src/androidMain/kotlin/com/mohamedrejeb/waypoint/sample/MainActivity.kt`
- Create: `sample/src/iosMain/kotlin/com/mohamedrejeb/waypoint/sample/MainViewController.kt`
- Create: `sample/src/jvmMain/kotlin/com/mohamedrejeb/waypoint/sample/main.kt`
- Create: `sample/src/jsMain/kotlin/com/mohamedrejeb/waypoint/sample/main.js.kt`
- Create: `sample/src/wasmJsMain/kotlin/com/mohamedrejeb/waypoint/sample/main.wasmJs.kt`

- [ ] **Step 1: Add new dependencies to version catalog**

Add to `gradle/libs.versions.toml`:

```toml
# In [versions] section, add:
nav3-runtime = "1.0.0"
nav3-ui = "1.0.0-alpha06"
kotlinx-serialization = "1.8.1"

# In [libraries] section, add:
androidx-navigation3-runtime = { module = "androidx.navigation3:navigation3-runtime", version.ref = "nav3-runtime" }
androidx-navigation3-ui = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "nav3-ui" }
kotlinx-serialization-core = { module = "org.jetbrains.kotlinx:kotlinx-serialization-core", version.ref = "kotlinx-serialization" }

# In [plugins] section, add:
kotlinx-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

- [ ] **Step 2: Update settings.gradle.kts**

Replace `include(":composeApp")` with `include(":sample")`.

- [ ] **Step 3: Create sample/build.gradle.kts**

Copy `composeApp/build.gradle.kts` as starting point, then modify:
- Add `alias(libs.plugins.kotlinx.serialization)` to plugins
- Change iOS framework `baseName` to `"WaypointSample"`
- Change Android `namespace` and `applicationId` to `"com.mohamedrejeb.waypoint.sample"`
- Change desktop `mainClass` to `"com.mohamedrejeb.waypoint.sample.MainKt"`
- Add Navigation3, serialization, and Material3 adaptive dependencies to commonMain:
  ```kotlin
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.kotlinx.serialization.core)
  ```
- Add `implementation(compose.materialIconsExtended)` to commonMain for full icon set
- Keep all existing dependencies (waypoint-core, waypoint-material3, compose.*, lifecycle.*)

- [ ] **Step 4: Move font resources**

Move `composeApp/src/commonMain/composeResources/font/` to `sample/src/commonMain/composeResources/font/` (the 4 Plus Jakarta Sans TTF files).

- [ ] **Step 5: Create platform entry points**

Create minimal entry points with package `com.mohamedrejeb.waypoint.sample`, all calling `App()`.

**Android** (`sample/src/androidMain/kotlin/com/mohamedrejeb/waypoint/sample/MainActivity.kt`):
```kotlin
package com.mohamedrejeb.waypoint.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
```

**iOS** (`sample/src/iosMain/kotlin/com/mohamedrejeb/waypoint/sample/MainViewController.kt`):
```kotlin
package com.mohamedrejeb.waypoint.sample

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App() }
```

**Desktop** (`sample/src/jvmMain/kotlin/com/mohamedrejeb/waypoint/sample/main.kt`):
```kotlin
package com.mohamedrejeb.waypoint.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Waypoint Sample",
    ) {
        App()
    }
}
```

**JS** (`sample/src/jsMain/kotlin/com/mohamedrejeb/waypoint/sample/main.js.kt`):
```kotlin
package com.mohamedrejeb.waypoint.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        App()
    }
}
```

**WasmJS** (`sample/src/wasmJsMain/kotlin/com/mohamedrejeb/waypoint/sample/main.wasmJs.kt`):
```kotlin
package com.mohamedrejeb.waypoint.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        App()
    }
}
```

- [ ] **Step 6: Create stub App.kt**

Create `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/App.kt`:
```kotlin
package com.mohamedrejeb.waypoint.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun App() {
    MaterialTheme {
        Text("Waypoint Sample — scaffold ready")
    }
}
```

- [ ] **Step 7: Verify build compiles**

Run: `./gradlew :sample:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Verify desktop runs**

Run: `./gradlew :sample:run`
Expected: Window appears with "Waypoint Sample — scaffold ready"

- [ ] **Step 9: Delete old composeApp module**

Delete the `composeApp/` directory entirely. Verify build still works:
Run: `./gradlew :sample:compileKotlinJvm`

- [ ] **Step 10: Commit**

```bash
git add sample/ settings.gradle.kts gradle/libs.versions.toml
git rm -rf composeApp/
git commit -m "chore: rename composeApp to sample, add Navigation3 and adaptive deps"
```

---

## Task 2: Theme — Colors, Typography, Material3 Theme

**Files:**
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/theme/Color.kt`
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/theme/Type.kt`
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/theme/Theme.kt`

- [ ] **Step 1: Create Color.kt**

```kotlin
package com.mohamedrejeb.waypoint.sample.theme

import androidx.compose.ui.graphics.Color

// Light scheme — seed: violet #7C3AED
val VioletPrimary = Color(0xFF7C3AED)
val VioletOnPrimary = Color(0xFFFFFFFF)
val VioletPrimaryContainer = Color(0xFFEDE7FE)
val VioletOnPrimaryContainer = Color(0xFF22005D)

val TealSecondary = Color(0xFF14B8A6)
val TealOnSecondary = Color(0xFFFFFFFF)
val TealSecondaryContainer = Color(0xFFB2F5EA)
val TealOnSecondaryContainer = Color(0xFF00201C)

val AmberTertiary = Color(0xFFF59E0B)
val AmberOnTertiary = Color(0xFFFFFFFF)
val AmberTertiaryContainer = Color(0xFFFFF3CD)
val AmberOnTertiaryContainer = Color(0xFF261A00)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFFCF8FF)
val OnBackgroundLight = Color(0xFF1C1B20)
val SurfaceLight = Color(0xFFFCF8FF)
val OnSurfaceLight = Color(0xFF1C1B20)
val SurfaceVariantLight = Color(0xFFE7E0EB)
val OnSurfaceVariantLight = Color(0xFF49454F)
val OutlineLight = Color(0xFF7A757F)
val OutlineVariantLight = Color(0xFFCBC4CF)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF7F2FA)
val SurfaceContainerLight = Color(0xFFF3EFF7)
val SurfaceContainerHighLight = Color(0xFFECE6F0)
val SurfaceContainerHighestLight = Color(0xFFE6E0E9)

// Dark scheme
val VioletPrimaryDark = Color(0xFFCFBCFF)
val VioletOnPrimaryDark = Color(0xFF3B0095)
val VioletPrimaryContainerDark = Color(0xFF5B17D1)
val VioletOnPrimaryContainerDark = Color(0xFFEDE7FE)

val TealSecondaryDark = Color(0xFF5EEAD4)
val TealOnSecondaryDark = Color(0xFF003731)
val TealSecondaryContainerDark = Color(0xFF005048)
val TealOnSecondaryContainerDark = Color(0xFFB2F5EA)

val AmberTertiaryDark = Color(0xFFFCD34D)
val AmberOnTertiaryDark = Color(0xFF3F2E00)
val AmberTertiaryContainerDark = Color(0xFF5B4300)
val AmberOnTertiaryContainerDark = Color(0xFFFFF3CD)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF141218)
val OnBackgroundDark = Color(0xFFE6E0E9)
val SurfaceDark = Color(0xFF141218)
val OnSurfaceDark = Color(0xFFE6E0E9)
val SurfaceVariantDark = Color(0xFF49454F)
val OnSurfaceVariantDark = Color(0xFFCBC4CF)
val OutlineDark = Color(0xFF948F99)
val OutlineVariantDark = Color(0xFF49454F)
val SurfaceContainerLowestDark = Color(0xFF0F0D13)
val SurfaceContainerLowDark = Color(0xFF1C1B20)
val SurfaceContainerDark = Color(0xFF211F26)
val SurfaceContainerHighDark = Color(0xFF2B2930)
val SurfaceContainerHighestDark = Color(0xFF36343B)
```

- [ ] **Step 2: Create Type.kt**

```kotlin
package com.mohamedrejeb.waypoint.sample.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import waypoint.sample.generated.resources.*
import waypoint.sample.generated.resources.Res

@Composable
fun PlusJakartaSansFamily(): FontFamily = FontFamily(
    Font(Res.font.PlusJakartaSans_Regular, FontWeight.Normal),
    Font(Res.font.PlusJakartaSans_Medium, FontWeight.Medium),
    Font(Res.font.PlusJakartaSans_SemiBold, FontWeight.SemiBold),
    Font(Res.font.PlusJakartaSans_Bold, FontWeight.Bold),
)

@Composable
fun SampleTypography(): Typography {
    val fontFamily = PlusJakartaSansFamily()
    val default = Typography()
    return Typography(
        displayLarge = default.displayLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
        displayMedium = default.displayMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
        displaySmall = default.displaySmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold),
        headlineLarge = default.headlineLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
        headlineMedium = default.headlineMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
        headlineSmall = default.headlineSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
        titleLarge = default.titleLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
        titleMedium = default.titleMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
        titleSmall = default.titleSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold),
        bodyLarge = default.bodyLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Normal),
        bodyMedium = default.bodyMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.Normal),
        bodySmall = default.bodySmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Normal),
        labelLarge = default.labelLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
        labelMedium = default.labelMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
        labelSmall = default.labelSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
    )
}
```

NOTE: The `Res.font.*` references depend on the font file names. Compose resources generates identifiers from file names replacing hyphens with underscores. The font files are `PlusJakartaSans-Regular.ttf` → `Res.font.PlusJakartaSans_Regular`. Verify the exact generated names after the first build and adjust if needed.

- [ ] **Step 3: Create Theme.kt**

```kotlin
package com.mohamedrejeb.waypoint.sample.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = VioletPrimary,
    onPrimary = VioletOnPrimary,
    primaryContainer = VioletPrimaryContainer,
    onPrimaryContainer = VioletOnPrimaryContainer,
    secondary = TealSecondary,
    onSecondary = TealOnSecondary,
    secondaryContainer = TealSecondaryContainer,
    onSecondaryContainer = TealOnSecondaryContainer,
    tertiary = AmberTertiary,
    onTertiary = AmberOnTertiary,
    tertiaryContainer = AmberTertiaryContainer,
    onTertiaryContainer = AmberOnTertiaryContainer,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
)

private val DarkColors = darkColorScheme(
    primary = VioletPrimaryDark,
    onPrimary = VioletOnPrimaryDark,
    primaryContainer = VioletPrimaryContainerDark,
    onPrimaryContainer = VioletOnPrimaryContainerDark,
    secondary = TealSecondaryDark,
    onSecondary = TealOnSecondaryDark,
    secondaryContainer = TealSecondaryContainerDark,
    onSecondaryContainer = TealOnSecondaryContainerDark,
    tertiary = AmberTertiaryDark,
    onTertiary = AmberOnTertiaryDark,
    tertiaryContainer = AmberTertiaryContainerDark,
    onTertiaryContainer = AmberOnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
)

@Composable
fun SampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = SampleTypography(),
        content = content,
    )
}
```

- [ ] **Step 4: Verify build**

Run: `./gradlew :sample:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/theme/
git commit -m "feat(sample): add Material3 theme with violet/teal palette and Plus Jakarta Sans"
```

---

## Task 3: Navigation Routes & App Root

**Files:**
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/navigation/Route.kt`
- Modify: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/App.kt`

- [ ] **Step 1: Create Route.kt**

```kotlin
package com.mohamedrejeb.waypoint.sample.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable data object Catalog : Route
    @Serializable data object Onboarding : Route
    @Serializable data object FeatureDiscovery : Route
    @Serializable data object InteractiveTutorial : Route
    @Serializable data object MultiTarget : Route
    @Serializable data object HighlightGallery : Route
    @Serializable data object ThemingPlayground : Route
    @Serializable data object AnalyticsDashboard : Route
    @Serializable data object ModalTours : Route
}
```

- [ ] **Step 2: Update App.kt with Navigation3**

Replace the stub `App.kt` with Navigation3 setup. This uses `rememberNavBackStack` and `NavDisplay` from Navigation3.

```kotlin
package com.mohamedrejeb.waypoint.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.mohamedrejeb.waypoint.sample.navigation.Route
import com.mohamedrejeb.waypoint.sample.theme.SampleTheme

@Composable
fun App() {
    SampleTheme {
        val backStack = rememberNavBackStack(Route.Catalog)

        NavDisplay(
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            entryProvider = entryProvider {
                entry<Route.Catalog> {
                    // Placeholder — replaced in Task 4
                    Text("Catalog Screen")
                }
                entry<Route.Onboarding> {
                    Text("Onboarding Demo")
                }
                entry<Route.FeatureDiscovery> {
                    Text("Feature Discovery Demo")
                }
                entry<Route.InteractiveTutorial> {
                    Text("Interactive Tutorial Demo")
                }
                entry<Route.MultiTarget> {
                    Text("Multi-Target Demo")
                }
                entry<Route.HighlightGallery> {
                    Text("Highlight Gallery Demo")
                }
                entry<Route.ThemingPlayground> {
                    Text("Theming Playground Demo")
                }
                entry<Route.AnalyticsDashboard> {
                    Text("Analytics Dashboard Demo")
                }
                entry<Route.ModalTours> {
                    Text("Modal Tours Demo")
                }
            },
        )
    }
}
```

NOTE: Navigation3 APIs are new and may differ slightly. The impl agent should read the actual library API. The core pattern is: `rememberNavBackStack(startKey)` creates the back stack, `NavDisplay` renders the current entry, and `backStack.add(route)` navigates forward. If API names differ (e.g., `NavEntry` vs `entry`), adapt accordingly. Check the actual imports from `androidx.navigation3.runtime` and `org.jetbrains.androidx.navigation3.ui`.

- [ ] **Step 3: Verify build and run**

Run: `./gradlew :sample:run`
Expected: Window shows "Catalog Screen" text

- [ ] **Step 4: Commit**

```bash
git add sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/
git commit -m "feat(sample): add Navigation3 routes and App root"
```

---

## Task 4: Catalog Screen & DemoCard

**Files:**
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/catalog/DemoCard.kt`
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/catalog/CatalogScreen.kt`
- Modify: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/App.kt`

- [ ] **Step 1: Create DemoCard.kt**

A visually attractive card with icon, title, description, and feature tag chips.

```kotlin
package com.mohamedrejeb.waypoint.sample.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class DemoItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val tags: List<String>,
)

@Composable
fun DemoCard(
    item: DemoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Icon in colored circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = item.iconTint.copy(alpha = 0.12f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.iconTint,
                    modifier = Modifier.size(24.dp),
                )
            }

            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Description
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Feature tags
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item.tags.forEach { tag ->
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Create CatalogScreen.kt**

Responsive grid: 1 column on compact, 2 on medium, 3 on expanded.

```kotlin
package com.mohamedrejeb.waypoint.sample.catalog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.waypoint.sample.navigation.Route
import com.mohamedrejeb.waypoint.sample.theme.AmberTertiary
import com.mohamedrejeb.waypoint.sample.theme.TealSecondary
import com.mohamedrejeb.waypoint.sample.theme.VioletPrimary

private val demos = listOf(
    DemoItem(
        title = "Onboarding Tour",
        description = "First-launch app walkthrough with spotlight, placements, and navigation",
        icon = Icons.Outlined.RocketLaunch,
        iconTint = VioletPrimary,
        tags = listOf("Spotlight", "Placement", "Navigation"),
    ),
    DemoItem(
        title = "Feature Discovery",
        description = "Persistent beacons that pulse to highlight new features",
        icon = Icons.Outlined.NewReleases,
        iconTint = TealSecondary,
        tags = listOf("Beacon", "Persistence", "Show Once"),
    ),
    DemoItem(
        title = "Interactive Tutorial",
        description = "Form walkthrough where steps advance on user interaction",
        icon = Icons.Outlined.School,
        iconTint = AmberTertiary,
        tags = listOf("AdvanceOn", "AllowClick", "Conditional"),
    ),
    DemoItem(
        title = "Multi-Target Spotlight",
        description = "Highlight groups of related elements simultaneously",
        icon = Icons.Outlined.FilterCenterFocus,
        iconTint = VioletPrimary,
        tags = listOf("Multi-Target", "Custom Tooltip"),
    ),
    DemoItem(
        title = "Highlight Gallery",
        description = "Visual showcase of every highlight style and shape",
        icon = Icons.Outlined.Palette,
        iconTint = TealSecondary,
        tags = listOf("Spotlight", "Pulse", "Border", "Ripple", "Custom"),
    ),
    DemoItem(
        title = "Theming Playground",
        description = "Customize tooltip colors, typography, and dimensions live",
        icon = Icons.Outlined.Tune,
        iconTint = AmberTertiary,
        tags = listOf("Theme", "Colors", "Typography"),
    ),
    DemoItem(
        title = "Analytics Dashboard",
        description = "Real-time event tracking for tour lifecycle",
        icon = Icons.Outlined.Analytics,
        iconTint = VioletPrimary,
        tags = listOf("Analytics", "Events", "Tour ID"),
    ),
    DemoItem(
        title = "Modal Tours",
        description = "Tours inside dialogs, sheets, and scrollable containers",
        icon = Icons.Outlined.Layers,
        iconTint = TealSecondary,
        tags = listOf("Dialog", "Sheet", "Auto-Scroll"),
    ),
)

private val routeForIndex: List<Route> = listOf(
    Route.Onboarding,
    Route.FeatureDiscovery,
    Route.InteractiveTutorial,
    Route.MultiTarget,
    Route.HighlightGallery,
    Route.ThemingPlayground,
    Route.AnalyticsDashboard,
    Route.ModalTours,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onDemoClick: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text("Waypoint")
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
        ) {
            items(demos.size) { index ->
                DemoCard(
                    item = demos[index],
                    onClick = { onDemoClick(routeForIndex[index]) },
                )
            }
        }
    }
}
```

NOTE: Some icon names (`RocketLaunch`, `NewReleases`, `FilterCenterFocus`, `Analytics`) require `material-icons-extended`. If any icon isn't found at compile time, replace with a similar icon from `Icons.Default` or `Icons.Outlined` that does exist. Common fallbacks: `PlayArrow`, `Star`, `Visibility`, `BarChart`.

- [ ] **Step 3: Wire CatalogScreen into App.kt**

Replace the `Route.Catalog` entry placeholder:
```kotlin
entry<Route.Catalog> {
    CatalogScreen(onDemoClick = { route -> backStack.add(route) })
}
```

Add import for `CatalogScreen`.

- [ ] **Step 4: Verify build and run**

Run: `./gradlew :sample:run`
Expected: Window shows the catalog grid with 8 demo cards. Clicking a card navigates to placeholder text. Back button/Escape returns to catalog.

- [ ] **Step 5: Commit**

```bash
git add sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/catalog/
git add sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/App.kt
git commit -m "feat(sample): add catalog home screen with demo cards grid"
```

---

## Task 5: DemoScaffold Shared Component

**Files:**
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/components/DemoScaffold.kt`

- [ ] **Step 1: Create DemoScaffold.kt**

Shared wrapper used by all 8 demos: top bar with back + title, description, "Start Tour" FAB.

```kotlin
package com.mohamedrejeb.waypoint.sample.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScaffold(
    title: String,
    description: String,
    onBack: () -> Unit,
    onStartTour: () -> Unit,
    modifier: Modifier = Modifier,
    fabVisible: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            if (fabVisible) {
                ExtendedFloatingActionButton(
                    onClick = onStartTour,
                    icon = {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                    },
                    text = { Text("Start Tour") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        modifier = modifier,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            // Description banner
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = 12.dp,
                ),
            )

            HorizontalDivider()

            // Demo content
            content(
                PaddingValues(bottom = padding.calculateBottomPadding()),
            )
        }
    }
}
```

- [ ] **Step 2: Verify build**

Run: `./gradlew :sample:compileKotlinJvm`

- [ ] **Step 3: Commit**

```bash
git add sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/components/
git commit -m "feat(sample): add DemoScaffold shared component"
```

---

## Task 6: Onboarding Tour Demo

**Files:**
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/onboarding/OnboardingDemo.kt`
- Modify: `App.kt` — replace placeholder entry

This demo builds a mock social/productivity app and runs a 6-step tour through it.

- [ ] **Step 1: Create OnboardingDemo.kt**

Build the complete demo composable. It should:
1. Create a mock app UI: top bar with search + bell icons, welcome card, stat cards row, task list, FAB, bottom nav
2. Define `WaypointState` with 6 steps covering different placements and shapes
3. Use `WaypointMaterial3Host` for the default Material3 tooltip
4. Wire the "Start Tour" button via `DemoScaffold`

Key Waypoint features to demonstrate:
- `rememberWaypointState { step(...) { ... } }` DSL
- `Modifier.waypointTarget(state, key)`
- `WaypointMaterial3Host` with `onTourComplete` and `onTourCancel`
- `TooltipPlacement.Top`, `.Bottom`, `.Start`, `.End`, `.Auto` (one per step)
- `SpotlightShape.Circle`, `.RoundedRect(12.dp)`, `.Pill`
- `SpotlightPadding(8.dp)` on one step
- `TargetInteraction.ClickToAdvance` on the FAB step

The mock UI should look polished — use Material3 cards, proper spacing, and the theme colors.

- [ ] **Step 2: Wire into App.kt**

Replace the `Route.Onboarding` entry:
```kotlin
entry<Route.Onboarding> {
    OnboardingDemo(onBack = { backStack.removeLastOrNull() })
}
```

- [ ] **Step 3: Verify build and run**

Run: `./gradlew :sample:run`
Navigate to Onboarding demo, tap "Start Tour", verify all 6 steps work.

- [ ] **Step 4: Commit**

```bash
git add sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/onboarding/
git add sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/App.kt
git commit -m "feat(sample): add onboarding tour demo"
```

---

## Task 7: Feature Discovery Demo (ViewModel + MVI)

**Files:**
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/discovery/DiscoveryContract.kt`
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/discovery/DiscoveryViewModel.kt`
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/discovery/FeatureDiscoveryDemo.kt`
- Modify: `App.kt`

- [ ] **Step 1: Create DiscoveryContract.kt**

MVI state and events:

```kotlin
package com.mohamedrejeb.waypoint.sample.demos.discovery

data class DiscoveryState(
    val hasSeenMessaging: Boolean = false,
    val hasSeenFilters: Boolean = false,
    val hasSeenDarkMode: Boolean = false,
)

sealed interface DiscoveryEvent {
    data object DismissMessaging : DiscoveryEvent
    data object DismissFilters : DiscoveryEvent
    data object DismissDarkMode : DiscoveryEvent
    data object ResetAll : DiscoveryEvent
}
```

- [ ] **Step 2: Create DiscoveryViewModel.kt**

```kotlin
package com.mohamedrejeb.waypoint.sample.demos.discovery

import androidx.lifecycle.ViewModel
import com.mohamedrejeb.waypoint.core.WaypointPersistence
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class DiscoveryViewModel : ViewModel() {
    private val _state = MutableStateFlow(DiscoveryState())
    val state: StateFlow<DiscoveryState> = _state

    val persistence = InMemoryPersistence()

    fun onEvent(event: DiscoveryEvent) {
        when (event) {
            DiscoveryEvent.DismissMessaging -> _state.update { it.copy(hasSeenMessaging = true) }
            DiscoveryEvent.DismissFilters -> _state.update { it.copy(hasSeenFilters = true) }
            DiscoveryEvent.DismissDarkMode -> _state.update { it.copy(hasSeenDarkMode = true) }
            DiscoveryEvent.ResetAll -> {
                _state.update { DiscoveryState() }
                persistence.resetAll()
            }
        }
    }
}

class InMemoryPersistence : WaypointPersistence {
    private val completed = mutableSetOf<String>()
    override fun isCompleted(tourId: String) = tourId in completed
    override fun markCompleted(tourId: String) { completed += tourId }
    override fun reset(tourId: String) { completed -= tourId }
    override fun resetAll() { completed.clear() }
}
```

- [ ] **Step 3: Create FeatureDiscoveryDemo.kt**

Build the demo with:
- 3 feature cards, each wrapped in `WaypointBeacon` with different styles:
  - "Smart Messaging" — `BeaconStyle.Pulse(color = violet)`
  - "Advanced Filters" — `BeaconStyle.Pulse(color = teal)`
  - "Dark Mode" — `BeaconStyle.Dot(color = amber)`
- Each beacon's `visible` bound to `!state.hasSeen*`
- Each beacon's `onClick` starts a 1-step tour for that feature and dispatches dismiss event
- Tour uses `WaypointPersistence` via the ViewModel
- "Reset All" button at the bottom
- Uses `DemoScaffold` (with `fabVisible = false` since beacons are the interaction)
- Feature cards should look polished with icons, titles, descriptions

Key Waypoint features: `WaypointBeacon`, `BeaconStyle.Pulse`, `BeaconStyle.Dot`, `WaypointPersistence`, `hasCompleted`, `resetCompletion`, `HighlightStyle.Border`, `HighlightStyle.Pulse`

- [ ] **Step 4: Wire into App.kt**

- [ ] **Step 5: Verify build and run**

- [ ] **Step 6: Commit**

```bash
git commit -m "feat(sample): add feature discovery demo with beacons and persistence"
```

---

## Task 8: Interactive Tutorial Demo (ViewModel + MVI)

**Files:**
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/tutorial/TutorialContract.kt`
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/tutorial/TutorialViewModel.kt`
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/tutorial/InteractiveTutorialDemo.kt`
- Modify: `App.kt`

- [ ] **Step 1: Create TutorialContract.kt**

```kotlin
package com.mohamedrejeb.waypoint.sample.demos.tutorial

data class TutorialState(
    val name: String = "",
    val email: String = "",
    val plan: Plan? = null,
    val agreedToTerms: Boolean = false,
    val isSubmitted: Boolean = false,
)

enum class Plan(val label: String, val price: String) {
    Free("Free", "$0/mo"),
    Pro("Pro", "$12/mo"),
    Enterprise("Enterprise", "$49/mo"),
}

sealed interface TutorialEvent {
    data class NameChanged(val value: String) : TutorialEvent
    data class EmailChanged(val value: String) : TutorialEvent
    data class PlanSelected(val plan: Plan) : TutorialEvent
    data class TermsToggled(val agreed: Boolean) : TutorialEvent
    data object Submit : TutorialEvent
    data object Reset : TutorialEvent
}
```

- [ ] **Step 2: Create TutorialViewModel.kt**

Standard MVI ViewModel reducing events to state updates.

- [ ] **Step 3: Create InteractiveTutorialDemo.kt**

Build a checkout/signup form with:
- Name `OutlinedTextField` — tour advances when name ≥ 2 chars via `WaypointTrigger.Custom`
- Email `OutlinedTextField` — advances when contains "@"
- Plan selector (3 selectable cards) — advances when plan selected
- Terms `Checkbox` — conditional step (`showIf { !state.agreedToTerms }`), `ClickToAdvance`
- Submit `Button` — `ClickToAdvance`
- All text fields use `TargetInteraction.AllowClick`
- Uses `onEnter`/`onExit` callbacks (e.g., log to snackbar)
- Uses `DemoScaffold`

Key Waypoint features: `WaypointTrigger.Custom` with `snapshotFlow`, `AllowClick`, `ClickToAdvance`, `showIf`, `onEnter`, `onExit`, ViewModel integration

- [ ] **Step 4-6: Wire, verify, commit**

```bash
git commit -m "feat(sample): add interactive tutorial demo with event-driven progression"
```

---

## Task 9: Multi-Target Spotlight Demo

**Files:**
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/multitarget/MultiTargetDemo.kt`
- Modify: `App.kt`

- [ ] **Step 1: Create MultiTargetDemo.kt**

Build a mock analytics dashboard:
- Header: logo icon + "Dashboard" text + date picker button + export button
- 4 KPI cards: Revenue ($24.5k), Users (1,847), Orders (384), Growth (+12.3%)
- Action bar: Filter, Sort, Download buttons

Tour (3 steps) using `additionalTargets` and custom tooltip content:
1. Highlight header group (logo + date + export) with custom tooltip showing "Dashboard Controls"
2. Highlight all 4 KPI cards simultaneously with custom tooltip showing mini descriptions
3. Highlight action bar buttons with custom tooltip

Each custom tooltip uses `step.content { scope -> ... }` with a rich composable (icons, columns, navigation buttons).

Key Waypoint features: `additionalTargets`, custom `content { }`, `StepScope`, `SpotlightShape.Pill`, `SpotlightShape.RoundedRect`, `SpotlightShape.Circle`

- [ ] **Step 2-4: Wire, verify, commit**

```bash
git commit -m "feat(sample): add multi-target spotlight demo"
```

---

## Task 10: Highlight Styles Gallery Demo

**Files:**
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/highlights/HighlightGalleryDemo.kt`
- Modify: `App.kt`

- [ ] **Step 1: Create HighlightGalleryDemo.kt**

Grid of 8 cards (using `LazyVerticalStaggeredGrid`), each showing one highlight style. Each card has a target element and a "Try It" button.

Cards:
1. **Spotlight (Circle)** — `HighlightStyle.Spotlight(shape = Circle)`
2. **Spotlight (RoundedRect)** — `HighlightStyle.Spotlight(shape = RoundedRect(12.dp), padding = SpotlightPadding(8.dp))`
3. **Spotlight (Pill)** — `HighlightStyle.Spotlight(shape = Pill)`
4. **Pulse** — `HighlightStyle.Pulse(color = teal, filled = true)`
5. **Border** — `HighlightStyle.Border(color = violet, borderWidth = 3.dp)`
6. **Ripple** — `HighlightStyle.Ripple(color = amber, ringCount = 3)`
7. **None** — `HighlightStyle.None` (tooltip only)
8. **Custom** — `HighlightStyle.Custom { targetBounds, animatedBounds -> ... }` with animated glow ring

Each "Try It" button creates a temporary 1-step `WaypointState` with that highlight style and starts it. The tour auto-dismisses on overlay click (`OverlayClickBehavior.Dismiss`).

Key Waypoint features: ALL `HighlightStyle` variants, ALL `SpotlightShape` variants, `SpotlightPadding`, `OverlayClickBehavior.Dismiss`

- [ ] **Step 2-4: Wire, verify, commit**

```bash
git commit -m "feat(sample): add highlight styles gallery demo"
```

---

## Task 11: Theming Playground Demo (ViewModel + MVI)

**Files:**
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/theming/ThemingContract.kt`
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/theming/ThemingViewModel.kt`
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/theming/ThemingPlaygroundDemo.kt`
- Modify: `App.kt`

- [ ] **Step 1: Create ThemingContract.kt**

```kotlin
package com.mohamedrejeb.waypoint.sample.demos.theming

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ThemingState(
    val tooltipBackground: Color = Color(0xFF1B1B2F),
    val titleColor: Color = Color.White,
    val descriptionColor: Color = Color(0xFFB0B0B0),
    val primaryButtonColor: Color = Color(0xFF7C3AED),
    val tooltipCornerRadius: Dp = 16.dp,
    val tooltipElevation: Dp = 8.dp,
    val tooltipPadding: Dp = 20.dp,
)

sealed interface ThemingEvent {
    data class BackgroundChanged(val color: Color) : ThemingEvent
    data class TitleColorChanged(val color: Color) : ThemingEvent
    data class DescriptionColorChanged(val color: Color) : ThemingEvent
    data class ButtonColorChanged(val color: Color) : ThemingEvent
    data class CornerRadiusChanged(val dp: Dp) : ThemingEvent
    data class ElevationChanged(val dp: Dp) : ThemingEvent
    data class PaddingChanged(val dp: Dp) : ThemingEvent
    data object ResetDefaults : ThemingEvent
}
```

- [ ] **Step 2: Create ThemingViewModel.kt**

Standard MVI ViewModel.

- [ ] **Step 3: Create ThemingPlaygroundDemo.kt**

Layout:
- **Controls section** (scrollable Column):
  - "Background" label + row of 6 color swatches (selectable)
  - "Title" label + row of color swatches
  - "Description" label + row of color swatches
  - "Button" label + row of color swatches
  - Corner radius `Slider` (0..32 dp)
  - Elevation `Slider` (0..24 dp)
  - Padding `Slider` (8..32 dp)
  - "Reset Defaults" `OutlinedButton`
- **Preview section**: 
  - A mock card with a target
  - "Start Themed Tour" button
- Tour (3 steps) wrapped in `WaypointMaterial3Theme` with colors/dims from ViewModel state

Key Waypoint features: `WaypointMaterial3Theme`, `WaypointMaterial3Colors`, `WaypointMaterial3Dimensions`, live theme switching

- [ ] **Step 4-6: Wire, verify, commit**

```bash
git commit -m "feat(sample): add theming playground demo"
```

---

## Task 12: Analytics Dashboard Demo (ViewModel + MVI)

**Files:**
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/analytics/AnalyticsContract.kt`
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/analytics/AnalyticsViewModel.kt`
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/analytics/AnalyticsDashboardDemo.kt`
- Modify: `App.kt`

- [ ] **Step 1: Create AnalyticsContract.kt**

```kotlin
package com.mohamedrejeb.waypoint.sample.demos.analytics

data class AnalyticsState(
    val events: List<AnalyticsLogEntry> = emptyList(),
)

data class AnalyticsLogEntry(
    val type: String,
    val details: String,
    val timestamp: Long = currentTimeMillis(),
)

sealed interface AnalyticsEvent {
    data class LogEvent(val type: String, val details: String) : AnalyticsEvent
    data object ClearLog : AnalyticsEvent
}

expect fun currentTimeMillis(): Long
```

NOTE: `currentTimeMillis()` needs `expect/actual`. Simplest: just use a counter or `kotlinx.datetime` if available. Alternatively, skip timestamps and use an incrementing index.

- [ ] **Step 2: Create AnalyticsViewModel.kt**

ViewModel that collects log entries. Also implements `WaypointAnalytics`:

```kotlin
class AnalyticsViewModel : ViewModel(), WaypointAnalytics {
    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state

    private fun log(type: String, details: String) {
        _state.update { current ->
            current.copy(events = current.events + AnalyticsLogEntry(type, details))
        }
    }

    fun onEvent(event: AnalyticsEvent) {
        when (event) {
            is AnalyticsEvent.ClearLog -> _state.update { it.copy(events = emptyList()) }
            is AnalyticsEvent.LogEvent -> log(event.type, event.details)
        }
    }

    // WaypointAnalytics implementation
    override fun onTourStarted(tourId: String?, totalSteps: Int) {
        log("Tour Started", "tourId=$tourId, steps=$totalSteps")
    }
    override fun onTourCompleted(tourId: String?, totalSteps: Int) {
        log("Tour Completed", "tourId=$tourId, steps=$totalSteps")
    }
    override fun onTourCancelled(tourId: String?, stepIndex: Int, totalSteps: Int) {
        log("Tour Cancelled", "tourId=$tourId, at step $stepIndex/$totalSteps")
    }
    override fun onStepViewed(tourId: String?, stepIndex: Int, targetKey: Any?) {
        log("Step Viewed", "tourId=$tourId, step=$stepIndex, key=$targetKey")
    }
    override fun onStepCompleted(tourId: String?, stepIndex: Int, targetKey: Any?) {
        log("Step Completed", "tourId=$tourId, step=$stepIndex, key=$targetKey")
    }
}
```

Actually, avoid the `expect/actual` complexity. Use a simple counter instead of timestamps:

```kotlin
data class AnalyticsLogEntry(
    val index: Int,
    val type: String,
    val details: String,
)
```

- [ ] **Step 3: Create AnalyticsDashboardDemo.kt**

Layout (vertical split):
- **Top**: Mock app with 4 targets (search bar, profile avatar, settings icon, main card). "Start Tour" / "Stop Tour" toggle. Tour uses `analytics = viewModel` and `tourId = "demo-analytics"`.
- **Bottom**: Event log panel with:
  - Header: "Event Log" + event count badge + "Clear" button
  - `LazyColumn` of event entries, each showing: index, type badge (color-coded by event type), details text
  - Auto-scrolls to bottom on new events

Key Waypoint features: `WaypointAnalytics` (all 5 methods), `tourId`, `analytics` param on `rememberWaypointState`

- [ ] **Step 4-6: Wire, verify, commit**

```bash
git commit -m "feat(sample): add analytics dashboard demo with real-time event log"
```

---

## Task 13: Modal Tours Demo

**Files:**
- Create: `sample/src/commonMain/kotlin/com/mohamedrejeb/waypoint/sample/demos/modals/ModalToursDemo.kt`
- Modify: `App.kt`

- [ ] **Step 1: Create ModalToursDemo.kt**

Three sections:

**Section 1 — Dialog Tour:**
- Button "Open Dialog with Tour"
- `Dialog` containing a settings-like form (2 switches + a slider)
- 2-step tour inside the dialog
- Demonstrates coordinate space handling

**Section 2 — Bottom Sheet Tour:**
- Button "Open Sheet with Tour"
- `ModalBottomSheet` with content (list of items)
- 2-step tour inside the sheet

**Section 3 — Scrollable Tour:**
- Tall scrollable `Column` with `verticalScroll(rememberScrollState())`
- Targets scattered at different scroll positions (top, middle, far bottom)
- 3-step tour with auto-scroll to off-screen targets
- Demonstrates `BringIntoViewRequester` auto-scroll and tooltip staying visible when target scrolls out

All 3 sections wrapped in `DemoScaffold`. Each has its own `WaypointState` and "Start" button.

Key Waypoint features: Dialog/Sheet coordinate handling, auto-scroll, scroll-out-of-view, `pause()`/`resume()`

- [ ] **Step 2-4: Wire, verify, commit**

```bash
git commit -m "feat(sample): add modal tours demo with dialog, sheet, and scroll"
```

---

## Task 14: Final Polish & Verification

- [ ] **Step 1: Run full build**

```bash
./gradlew :sample:compileKotlinJvm :waypoint-core:jvmTest :waypoint-material3:jvmTest
```

Expected: BUILD SUCCESSFUL, 257 tests pass

- [ ] **Step 2: Run desktop app and test all 8 demos**

```bash
./gradlew :sample:run
```

Walk through each demo:
1. Catalog grid renders responsively
2. Each card navigates to its demo
3. Back navigation works (predictive back on supported platforms)
4. Each demo's tour starts and runs correctly
5. Dark theme works (toggle system theme)
6. Window resizing changes card grid layout (responsive)

- [ ] **Step 3: Fix any compilation or runtime issues found**

- [ ] **Step 4: Final commit**

```bash
git add -A
git commit -m "feat(sample): complete sample app rework with 8 catalog demos"
```
