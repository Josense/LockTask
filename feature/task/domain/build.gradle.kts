plugins {
    alias(libs.plugins.ktlint)
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) }
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    testImplementation(libs.junit)
    implementation(libs.kotlinx.coroutines.core)
}

ktlint {
    version.set(libs.versions.ktlint)
    android.set(false)
    outputToConsole.set(true)
    filter { exclude("**/build/**") }
}
