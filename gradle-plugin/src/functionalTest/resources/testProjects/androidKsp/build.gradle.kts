plugins {
    id("com.android.application") version "8.5.1"
    id("io.github.schwarzit.lightsaber")
    kotlin("android") version "1.9.25"
    id("com.google.devtools.ksp") version "1.9.25-1.0.20"
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
    implementation("com.google.dagger:dagger:2.52")
    ksp("com.google.dagger:dagger-compiler:2.52")
}
