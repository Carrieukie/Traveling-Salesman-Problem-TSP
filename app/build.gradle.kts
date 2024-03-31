@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.kotlin)
    alias(libs.plugins.hilt.android)
    kotlin("kapt")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.karis.travellingsalesman"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.karis.travellingsalesman"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(libs.bundles.compose)
    implementation(libs.maps.compose)
    implementation(libs.google.maps.services)
    implementation(libs.timber)
    implementation(libs.androidx.material3.v120alpha02)

    implementation(libs.accompanist.systemuicontroller)

    //Retrofit
    implementation (libs.retrofit)
    //Gson
    implementation (libs.converter.gson)
    //LoggingInterceptor
    implementation (libs.logging.interceptor)

    // coil
    implementation(libs.coil.compose)


    // hilt
    api(libs.hilt.android)
    implementation(libs.androidx.appcompat)
    debugImplementation(libs.compose.ui.tooling)
    kapt(libs.hilt.android.compiler)
    api(libs.androidx.hilt.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

secrets {
    // To add your Maps API key to this project:
    // 1. Open the file local.properties found under the root project
    // 2. Add this line, where YOUR_API_KEY is your API key:
    //        MAPS_API_KEY=YOUR_API_KEY
    defaultPropertiesFileName = "local.properties"
}