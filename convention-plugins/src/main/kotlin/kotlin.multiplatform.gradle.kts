import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("android.library")
}

kotlin {
    explicitApi()
    applyHierarchyTemplate()
    applyTargets()

    // Public API baseline in `api/`. `checkKotlinAbi` runs as part of `check`,
    // `updateKotlinAbi` re-records the dump after an intended API change.
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        enabled.set(true)
    }
}

setJvmTarget()
