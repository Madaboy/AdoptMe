// ============================================================
// build.gradle.kts  (nivel de módulo: app)
// AdoptMe — Frontend MVP con Jetpack Compose
// ============================================================

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.adoptme"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.adoptme"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1-mvp"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // -------------------------------------------------------
    // COMPOSE — UI declarativa (requerida)
    // -------------------------------------------------------
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // -------------------------------------------------------
    // VIEWMODEL + LIFECYCLE (requerida para MVVM)
    // -------------------------------------------------------
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // -------------------------------------------------------
    // CORE ANDROID
    // -------------------------------------------------------
    implementation(libs.androidx.core.ktx)

    // -------------------------------------------------------
    // NAVIGATION & PERSISTENCE
    // -------------------------------------------------------
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    // -------------------------------------------------------
    // TESTING
    // -------------------------------------------------------
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // -------------------------------------------------------
    // NETWORKING & IMAGES (Para Huachitos API)
    // -------------------------------------------------------
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.coil.compose)

    // -------------------------------------------------------
    // HTML de descripciones (Huachitos entrega <p> y <br>)
    // -------------------------------------------------------
    implementation(libs.jsoup)
}
