plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "dev.omerdurmaz.bookshelf"
    compileSdk { version = release(37) }

    defaultConfig {
        applicationId = "dev.omerdurmaz.bookshelf"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }

    testOptions { unitTests.isIncludeAndroidResources = true } // Robolectric
}

dependencies {
    implementation("androidx.navigation3:navigation3-runtime:1.2.1")
    implementation("androidx.navigation3:navigation3-ui:1.2.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:2.11.0")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation3:1.3.0")

    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.material3:material3")
    // Bottom bar on phones, navigation rail on wider windows.
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.18.1")
    testImplementation("androidx.test:core:1.7.0")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
