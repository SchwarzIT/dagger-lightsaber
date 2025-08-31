plugins {
    id("com.android.application") version "8.12.2"
    id("io.github.schwarzit.lightsaber")
    kotlin("android") version "2.2.10"
    kotlin("kapt") version "2.2.10"
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
    implementation("com.google.dagger:dagger:2.57.1")
    kapt("com.google.dagger:dagger-compiler:2.57.1")
}
