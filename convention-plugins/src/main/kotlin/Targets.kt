import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("OPT_IN_USAGE")
fun KotlinMultiplatformExtension.applyTargets() {
    jvm()

    js {
        browser {
            // Compose UI tests can't bootstrap Skiko on the legacy k/js Karma
            // runner (org_jetbrains_skia_* symbols unresolved). The same suite
            // runs on jvm, wasmJs (ChromeHeadless), and iOS simulator, so js
            // execution adds no coverage. Compilation still runs.
            testTask {
                enabled = false
            }
        }
        // Compose UI tests need the webpack bundle to load Skiko, and the
        // Compose plugin fails the build without a declared executable.
        binaries.executable()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    iosArm64()
    iosSimulatorArm64()
}
