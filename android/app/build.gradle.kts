plugins {
    id("com.android.application")
}

android {
    namespace = "nl.nederlandstijd.widget"
    compileSdk = 37

    defaultConfig {
        applicationId = "nl.nederlandstijd.widget"
        minSdk = 23
        targetSdk = 37
        versionCode = 6
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.annotation:annotation:1.9.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
}
