plugins {
    id("compose.multiplatform")
    id("module.publication")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.ui)
            implementation(libs.compose.animation)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.compose.uiTest)
        }
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

tasks.withType<Test> {
    systemProperty("golden.dir", file("src/jvmTest/resources/goldens").absolutePath)
}
