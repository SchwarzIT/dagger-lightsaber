plugins {
    id("com.android.application") version "8.13.2"
    id("io.github.schwarzit.lightsaber")
    kotlin("android") version "2.3.10"
    kotlin("kapt") version "2.3.10"
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
    implementation("com.google.dagger:dagger:2.59.2")
    kapt("com.google.dagger:dagger-compiler:2.59.2")
}
