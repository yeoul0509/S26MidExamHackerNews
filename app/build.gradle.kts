plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.21"
}

android {
    namespace = "kr.ac.kumoh.s20220636.s26midexamhackernews"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "kr.ac.kumoh.s20220636.s26midexamhackernews"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Navigation 3
    // https://developer.android.com/jetpack/androidx/releases/navigation3?hl=ko
    val nav3Version = "1.2.0"
    implementation("androidx.navigation3:navigation3-runtime:$nav3Version")
    implementation("androidx.navigation3:navigation3-ui:$nav3Version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:2.11.0")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation3:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")

    // Retrofit 3
    // https://github.com/lysine-dev/retrofit/releases
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:3.0.0")

    // AsyncImage
    // https://github.com/coil-kt/coil/blob/main/README-ko.md
    // https://github.com/coil-kt/coil/releases
    implementation("io.coil-kt.coil3:coil-compose:3.6.3")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.6.3")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

}