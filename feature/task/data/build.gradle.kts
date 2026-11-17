plugins {
    alias(libs.plugins.ktlint)
    id("com.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.josense.locktask.task.data"
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
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    implementation(project(":task:domain"))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.koin.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}

ktlint {
    version.set(libs.versions.ktlint)
    android.set(true)
    outputToConsole.set(true)
    filter { exclude("**/build/**") }
}
