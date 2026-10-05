plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("android.library")
}

kotlin {
    explicitApi()
    applyHierarchyTemplate()
    applyTargets()
}

setJvmTarget()
