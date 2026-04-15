import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("OPT_IN_USAGE")
fun KotlinMultiplatformExtension.applyTargets() {
    androidTarget {
        publishLibraryVariants("release")
    }

    jvm()

    js {
        browser()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    iosArm64()
    iosSimulatorArm64()
}
