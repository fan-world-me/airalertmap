plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val releaseSigningValues = listOf(
    providers.gradleProperty("AIRALERT_KEYSTORE_PATH")
        .orElse(providers.environmentVariable("AIRALERT_KEYSTORE_PATH"))
        .orNull,
    providers.gradleProperty("AIRALERT_KEYSTORE_PASSWORD")
        .orElse(providers.environmentVariable("AIRALERT_KEYSTORE_PASSWORD"))
        .orNull,
    providers.gradleProperty("AIRALERT_KEY_ALIAS")
        .orElse(providers.environmentVariable("AIRALERT_KEY_ALIAS"))
        .orNull,
    providers.gradleProperty("AIRALERT_KEY_PASSWORD")
        .orElse(providers.environmentVariable("AIRALERT_KEY_PASSWORD"))
        .orNull
)
val hasReleaseSigning = releaseSigningValues.all { !it.isNullOrBlank() }
val hasPartialReleaseSigning = releaseSigningValues.any { !it.isNullOrBlank() } && !hasReleaseSigning

if (hasPartialReleaseSigning) {
    throw GradleException(
        "Release signing requires AIRALERT_KEYSTORE_PATH, AIRALERT_KEYSTORE_PASSWORD, " +
            "AIRALERT_KEY_ALIAS, and AIRALERT_KEY_PASSWORD."
    )
}

android {
    namespace = "com.airalertmap"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.airalertmap"
        minSdk = 26
        targetSdk = 34
        versionCode = providers.gradleProperty("releaseVersionCode").orNull?.toIntOrNull() ?: 2
        versionName = providers.gradleProperty("releaseVersionName").orNull ?: "1.0.0"
    }

    buildFeatures { compose = true }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions { jvmTarget = "17" }

    if (hasReleaseSigning) {
        signingConfigs {
            create("release") {
                storeFile = file(releaseSigningValues[0]!!)
                storePassword = releaseSigningValues[1]
                keyAlias = releaseSigningValues[2]
                keyPassword = releaseSigningValues[3]
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}
