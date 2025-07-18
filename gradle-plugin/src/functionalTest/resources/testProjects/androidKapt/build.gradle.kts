plugins {
    id("com.android.application") version "8.11.1"
    id("io.github.schwarzit.lightsaber")
    kotlin("android") version "2.1.21"
    kotlin("kapt") version "2.1.21"
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
    implementation("com.google.dagger:dagger:2.53")
    kapt("com.google.dagger:dagger-compiler:2.53")
}
