plugins {
    id("com.android.application") version "8.13.2"
    id("io.github.schwarzit.lightsaber")
    kotlin("android") version "2.2.20"
    id("com.google.devtools.ksp") version "2.3.4"
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
    implementation("com.google.dagger:dagger:2.59.1")
    ksp("com.google.dagger:dagger-compiler:2.59.1")
}
