plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    id("com.google.devtools.ksp")

    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.my.mapupassessment"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.my.mapupassessment"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField(type = "String", name = "BASE_URL", value = "\"https://apis.tollguru.com/toll/v2/\"" )
            buildConfigField(type = "String", name = "API_KEY", value = "\"tt_529A87935E9E4D44B01E366C94D28FA1\"" )
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true

            buildConfigField(type = "String", name = "BASE_URL", value = "\"https://apis.tollguru.com/toll/v2/\"" )
            buildConfigField(type = "String", name = "API_KEY", value = "\"tt_529A87935E9E4D44B01E366C94D28FA1\"" )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures{
        viewBinding = true
        buildConfig = true
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.play.services.location)
//    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //    Permission
    implementation(libs.kotlin.permission)

    //    Hilt-Dagger
    implementation (libs.hilt.android)
    ksp (libs.hilt.compiler)

    // design dynamic
    implementation (libs.ssp.android)
    implementation (libs.sdp.android)

    //    Room Database
    implementation (libs.androidx.room.runtime)
    implementation (libs.androidx.room.ktx)
    ksp (libs.androidx.room.compiler)

    //    Lifecycles
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.livedata.ktx)

    //    Coroutines
    implementation (libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)

    // Retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.okhttp)
    implementation (libs.logging.interceptor)

    implementation("org.osmdroid:osmdroid-android:6.1.20")

    implementation("androidx.recyclerview:recyclerview:1.4.0")

}