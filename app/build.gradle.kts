plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.adminlivria"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.adminlivria"
        minSdk = 29
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.4"

        val apiBaseRaw = System.getenv("API_BASE")?.trim()
            ?: (project.findProperty("API_BASE") as String?)?.trim()
            ?: rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use {
                java.util.Properties().apply { load(it) }.getProperty("API_BASE")?.trim()
            }
            ?: ""
        val apiHost = apiBaseRaw.trimEnd('/').let { if (it.isEmpty()) "https://livriabackend-g5afdubmcxfacjbe.chilecentral-01.azurewebsites.net" else it }
        val baseUrl = when {
            apiHost.endsWith("/api/v1", ignoreCase = true) ->
                if (apiHost.endsWith("/")) apiHost else "$apiHost/"
            else -> "$apiHost/api/v1/"
        }
        fun escapeForBuildConfig(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")
        buildConfigField("String", "BASE_URL", "\"${escapeForBuildConfig(baseUrl)}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Firma con debug: el APK release se puede instalar sin keystore de producción.
            // (Sin esto suele generarse unsigned y el sistema muestra "App not installed".)
            // Para Play Store: signingConfig = signingConfigs.getByName("release") + keystore.
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation.layout)

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation(libs.androidx.foundation)

    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    implementation("androidx.navigation:navigation-compose:2.9.4")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")


    implementation("io.insert-koin:koin-android:3.5.0")
    implementation("com.google.dagger:hilt-android:2.50")

    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")

    implementation("androidx.compose.material:material-icons-extended")
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-analytics")
}