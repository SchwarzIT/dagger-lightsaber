plugins {
    id("com.android.application") version "8.10.0"
    id("io.github.schwarzit.lightsaber")
    kotlin("android") version "2.0.21"
    kotlin("kapt") version "2.0.21"
}

android {
    compileSdk = 33
    namespace = "test.namespace"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.google.dagger:dagger:2.48.1")
    kapt("com.google.dagger:dagger-compiler:2.48.1")
}
