import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
}

// Read GOOGLE_WEB_CLIENT_ID from local.properties (never committed) so the
// secret stays out of source control.
val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) FileInputStream(f).use { load(it) }
}
val googleWebClientId: String = localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")

android {
    namespace = "com.example.hroes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bina.hris"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
    }

    buildTypes {
        debug {
            // Debug → server BINA HR production (app.bina-hris.com = Laravel API gateway)
            buildConfigField("String", "API_BASE_URL", "\"https://app.bina-hris.com/api/\"")
            // Multi-tenant build: TENANT_DOMAIN kosong → user input kode perusahaan di TenantSetupScreen
            buildConfigField("String", "TENANT_DOMAIN", "\"\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Production environment — BINA HR (app.bina-hris.com = Laravel API gateway)
            buildConfigField("String", "API_BASE_URL", "\"https://app.bina-hris.com/api/\"")
            // Multi-tenant build: TENANT_DOMAIN kosong → user input kode perusahaan di TenantSetupScreen
            buildConfigField("String", "TENANT_DOMAIN", "\"\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin {
        jvmToolchain(17)
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.core.ktx)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material) // for pull-refresh + swipe-to-dismiss
    implementation(libs.androidx.material.icons.extended)
    
    // Navigation
    implementation(libs.navigation.compose)
    
    // Lottie Animations
    implementation(libs.lottie.compose)
    
    // Image Loading
    implementation(libs.coil.compose)
    
    // Play Services Location
    implementation(libs.play.services.location)

    // Retrofit + OkHttp (backend API)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Credential Manager — Google Sign-In (modern API)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Biometric authentication (re-login via fingerprint/face)
    implementation(libs.androidx.biometric)

    // CameraX — selfie capture for attendance
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Permissions helper for Jetpack Compose
    implementation(libs.accompanist.permissions)

    // EncryptedSharedPreferences (secure token storage)
    implementation(libs.security.crypto)

    // ViewModel + Lifecycle for Compose
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.ktx)

    // Firebase (FCM push notifications)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
