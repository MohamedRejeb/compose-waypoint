import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("OPT_IN_USAGE")
fun KotlinMultiplatformExtension.applyTargets() {
    androidTarget {
        publishLibraryVariants("release")
    }

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
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    iosArm64()
    iosSimulatorArm64()
}
