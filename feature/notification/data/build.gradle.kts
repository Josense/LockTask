plugins {
    alias(libs.plugins.ktlint)
    id("com.android.library")
}

android {
    namespace = "com.josense.locktask.notification.data"
    compileSdk { version = release(37) }
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    testImplementation(libs.junit)
    implementation(project(":notification:domain"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.koin.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}

ktlint {
    version.set(libs.versions.ktlint)
    android.set(true)
    outputToConsole.set(true)
    filter { exclude("**/build/**") }
}
