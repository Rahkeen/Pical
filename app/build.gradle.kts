plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "dev.supergooey.caloriesnap"
  compileSdk = 35

  defaultConfig {
    applicationId = "dev.supergooey.caloriesnap"
    minSdk = 33
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
        "retrofit.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
  }
  buildFeatures {
    compose = true
  }
}

composeCompiler {
  enableStrongSkippingMode = true
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.shapes)

  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.animation)

  implementation(libs.camerax)
  implementation(libs.camerax.view)
  implementation(libs.camerax.compose.viewfinder)

  implementation(libs.retrofit)
  implementation(libs.okhttp)
  implementation(libs.kotlin.serialization.json)
  implementation(libs.retrofit.kotlin.serialization)

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)

  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}