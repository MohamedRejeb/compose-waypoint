plugins {
    id("compose.multiplatform")
    id("module.publication")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":waypoint-core"))
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.compose.material3)
            implementation(libs.compose.foundation)
            implementation(libs.compose.animation)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}
