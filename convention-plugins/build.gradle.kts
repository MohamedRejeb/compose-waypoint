plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

group = "com.mohamedrejeb.gradle"
version = "0.1.0"

dependencies {
    implementation(libs.gradlePlugin.android)
    implementation(libs.gradlePlugin.jetbrainsCompose)
    implementation(libs.gradlePlugin.kotlin)
    implementation(libs.gradlePlugin.composeCompiler)
    implementation(libs.vanniktech.maven.publish)
    // Hack to access version catalog from precompiled script plugins
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
