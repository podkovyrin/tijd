plugins {
    id("com.android.application")
}

android {
    namespace = "nl.nederlandstijd.widget"
    compileSdk = 37
    ndkVersion = "28.1.13356709"

    defaultConfig {
        applicationId = "nl.nederlandstijd.widget"
        minSdk = 23
        targetSdk = 37
        versionCode = 6
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Supply an existing upload key through the environment; never commit key material.
    val uploadStore = providers.environmentVariable("ANDROID_UPLOAD_KEYSTORE").orNull
    if (uploadStore != null) {
        signingConfigs.create("upload") {
            storeFile = file(uploadStore)
            storePassword = providers.environmentVariable("ANDROID_UPLOAD_STORE_PASSWORD").get()
            keyAlias = providers.environmentVariable("ANDROID_UPLOAD_KEY_ALIAS").get()
            keyPassword = providers.environmentVariable("ANDROID_UPLOAD_KEY_PASSWORD").get()
        }
        buildTypes.getByName("release").signingConfig = signingConfigs.getByName("upload")
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "4.1.2"
        }
    }

    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.annotation:annotation:1.9.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
}
