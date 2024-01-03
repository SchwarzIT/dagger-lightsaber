plugins {
    id("com.android.application") version "8.2.1"
    id("io.github.schwarzit.lightsaber")
    kotlin("android") version "1.9.22"
    kotlin("kapt") version "1.9.22"
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
