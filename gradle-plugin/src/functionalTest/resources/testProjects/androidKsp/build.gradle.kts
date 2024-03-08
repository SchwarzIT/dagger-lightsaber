plugins {
    id("com.android.application") version "8.3.0"
    id("io.github.schwarzit.lightsaber")
    kotlin("android") version "1.9.23"
    id("com.google.devtools.ksp") version "1.9.23-1.0.19"
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
