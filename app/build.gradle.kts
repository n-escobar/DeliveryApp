plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.deliveryapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ADD THIS SECTION
    flavorDimensions += "userType"

    productFlavors {
        create("shopper") {
            dimension = "userType"
            applicationId = "com.example.deliveryapp.shopper"
            versionNameSuffix = "-shopper"

            // Custom configuration
            manifestPlaceholders["appName"] = "Delivery Shop"
            //buildConfigField("String", "USER_TYPE", "\"SHOPPER\"")

            // Optional: different version code for tracking
            //versionCode = 1
        }

        create("deliverer") {
            dimension = "userType"
            applicationId = "com.example.deliveryapp.deliverer"
            versionNameSuffix = "-deliverer"

            manifestPlaceholders["appName"] = "Delivery Driver"
            //buildConfigField("String", "USER_TYPE", "\"DELIVERER\"")

            //versionCode = 2
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true  // ADD THIS
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // Existing dependencies...
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM (Bill of Materials)
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Optional: Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Unit Testing - ADD THESE
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Optional but recommended
    testImplementation("io.mockk:mockk:1.13.8")  // For mocking
    testImplementation("app.cash.turbine:turbine:1.0.0")  // For Flow testing
    testImplementation("com.google.truth:truth:1.1.5")  // Better assertions

    /*DEPENDENCIES DIRECTLY PROVIDED BY FIREBASE WHEN SETTING UP PROJECT

    //Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.8.0"))

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries*/

    // ═══════════════════════════════════════════════════
    // FIREBASE DEPENDENCIES (from RA3 comprehensive Firebase guide artifact)
    // ═══════════════════════════════════════════════════

    // Firebase BoM (Bill of Materials) - manages versions
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")

    // Firestore Database
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Realtime Database (alternative to Firestore)
    // implementation("com.google.firebase:firebase-database-ktx")

    // Cloud Storage
    implementation("com.google.firebase:firebase-storage-ktx")

    // Cloud Messaging (Push Notifications)
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Firebase UI for Auth (optional, makes auth easier)
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")

    // Coroutines Play Services (for Firebase + Coroutines)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}

