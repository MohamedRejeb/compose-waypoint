plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("android.library")
}

kotlin {
    applyHierarchyTemplate()
    applyTargets()
}

setJvmTarget()
