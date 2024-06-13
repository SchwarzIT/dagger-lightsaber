plugins {
    id("com.android.application") version "8.5.0"
    id("io.github.schwarzit.lightsaber")
    kotlin("android") version "1.9.24"
    id("com.google.devtools.ksp") version "1.9.24-1.0.20"
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
    ksp("com.google.dagger:dagger-compiler:2.48.1")
}
