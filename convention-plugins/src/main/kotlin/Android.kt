import com.android.build.gradle.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the

fun Project.androidLibrarySetup() {
    val libs = the<LibrariesForLibs>()

    extensions.configure<LibraryExtension> {
        namespace = group.toString() + path.replace("-", "").split(":").joinToString(".")
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        defaultConfig {
            minSdk = libs.versions.android.minSdk.get().toInt()
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        // commonTest contains Compose UI tests (runComposeUiTest), which cannot
        // run on the local Android unit-test JVM (stubbed android.jar, no
        // Robolectric): every UI test dies on Build.FINGERPRINT == null. The
        // exact same suite runs on the jvm target (jvmTest), so Android-local
        // execution only duplicates the pure tests and breaks on the UI ones.
        testOptions {
            unitTests.all { it.isEnabled = false }
        }
    }
}
