import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

val libs = the<LibrariesForLibs>()

kotlin {
    explicitApi()
    applyHierarchyTemplate()
    applyTargets()

    // No host tests are declared for Android on purpose: commonTest holds Compose
    // UI tests, which cannot run on the local Android unit-test JVM. The same
    // suite runs on the jvm target.
    android {
        namespace = group.toString() + path.replace("-", "").split(":").joinToString(".")
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    // Public API baseline in `api/`. `checkKotlinAbi` runs as part of `check`,
    // `updateKotlinAbi` re-records the dump after an intended API change.
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()
}

setJvmTarget()
