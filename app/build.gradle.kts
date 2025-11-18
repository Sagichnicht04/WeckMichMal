plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}


android {
    namespace = "de.heinzenburger.g2_weckmichmal"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.heinzenburger.g2_weckmichmal"
        minSdk = 31
        targetSdk = 36
        versionCode = 17
        versionName = "2.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }

}

dependencies {
    implementation(libs.accompanist.drawablepainter)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    androidTestImplementation(libs.androidx.compose.testing)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.room.runtime)
    implementation(libs.material3)

    // ICS parsing lib
    implementation(libs.biweekly)

    // HTML parsing lib
    implementation(libs.jsoup)

    // web requests for uploading the log file
    implementation(libs.okhttp)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.mockito.kotlin)

    // Compose UI base dependencies
    implementation(libs.ui)
    implementation(libs.androidx.material)

    // Optional, for tooling support
    implementation(libs.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.tooling)
    ksp(libs.androidx.room.room.compiler)

    // for JSON parsing
    testImplementation(libs.json)
    implementation(libs.gson)
    implementation(libs.androidx.lifecycle.viewmodel.compose)



}