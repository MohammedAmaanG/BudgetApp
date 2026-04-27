import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)   // Required by Room annotation processor (KSP replaces KAPT)
}

android {
    namespace = "com.prog7313.budgetapp"
    // compileSdk 34 matches AGP 8.5.0 — avoids the "tested up to 34" warning.
    // To use 35 anyway, add:  android.suppressUnsupportedCompileSdk=35  in gradle.properties
    compileSdk = 34

    defaultConfig {
        applicationId = "com.prog7313.budgetapp"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL",
            "\"${localProperties.getProperty("SUPABASE_URL", "https://placeholder.supabase.co")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY",
            "\"${localProperties.getProperty("SUPABASE_ANON_KEY", "placeholder")}\"")
        buildConfigField("String", "AIRTABLE_API_KEY",
            "\"${localProperties.getProperty("AIRTABLE_API_KEY", "placeholder")}\"")
        buildConfigField("String", "AIRTABLE_BASE_ID",
            "\"${localProperties.getProperty("AIRTABLE_BASE_ID", "placeholder")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.splashscreen)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    debugImplementation(libs.androidx.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // ── Room — local SQLite database for Part 2 ───────────────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)          // suspend + Flow support for Room DAOs
    ksp(libs.room.compiler)                // KSP annotation processor (replaces kapt)


    // ── HTTP: Retrofit + OkHttp (used for BOTH Supabase REST AND Airtable) ──
    // No Supabase Kotlin SDK needed — we call Supabase's REST API directly
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Kotlinx Serialization (used in domain models)
    implementation(libs.kotlinx.serialization.json)

    // Image loading
    implementation(libs.coil.compose)

    // Misc
    implementation(libs.coroutines.android)
    implementation(libs.datastore)
    implementation(libs.accompanist.permissions)
}