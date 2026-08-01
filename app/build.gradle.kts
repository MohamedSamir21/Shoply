plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    alias(libs.plugins.android.junit5) // teaches AGP to run JUnit 5 (Jupiter) tests on src/test
    alias(libs.plugins.dokka)          // `./gradlew :app:dokkaHtml` renders KDoc -> HTML docs
}

android {
    namespace = "com.shopping.shoply"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.shopping.shoply"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("tempDebug") {
            storeFile = file("temp-debug.keystore")
            storePassword = "android"
            keyAlias = "tempkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("tempDebug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    testOptions {
        unitTests {
            // Robolectric needs the merged Android resources/manifest available to JVM tests.
            isIncludeAndroidResources = true
            // For plain (non-Robolectric) tests: calls into android.util.* return defaults
            // (0/null/false) instead of throwing "Method ... not mocked". Lets us unit-test
            // classes that incidentally log without dragging in the whole framework.
            isReturnDefaultValues = true
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.animation.graphics.android)
    testImplementation(libs.junit.jupiter)
    // Konsist: architecture rules enforced as plain unit tests (test-only, not shipped).
    testImplementation(libs.konsist)

    // --- Testing tutorial stack (all test-only, never shipped) ---
    testImplementation(libs.junit.jupiter.api)      // @Test, assertions, lifecycle annotations
    testImplementation(libs.junit.jupiter.params)   // @ParameterizedTest, @MethodSource, etc.
    testRuntimeOnly(libs.junit.jupiter.engine)      // the engine that actually runs Jupiter tests
    testImplementation(libs.mockk)                  // mock collaborators (FirebaseAuth, Firestore, ...)
    testImplementation(libs.kotlinx.coroutines.test) // runTest + TestDispatchers for suspend code
    testImplementation(libs.turbine)                // collect/await Flow emissions in tests
    testImplementation(libs.truth)                  // assertThat(...).isEqualTo(...) assertions
    testImplementation(libs.robolectric)            // real Android framework on the JVM (Patterns, etc.)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Google Fonts
    implementation(libs.androidx.ui.text.google.fonts)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coil
    implementation(libs.coil.compose)

    // Splash API
    implementation(libs.androidx.core.splashscreen)

    // Datastore
    implementation(libs.androidx.datastore.preferences)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.kotlinx.coroutines.play.services)

    // Adaptive
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.material3.adaptive)

    // Credentials
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Facebook Login
    implementation(libs.facebook.login)

    // Google Lib Phone Number
    implementation(libs.libphonenumber)

    // Password strength estimation (Nulab's zxcvbn4j port)
    implementation(libs.zxcvbn)
}