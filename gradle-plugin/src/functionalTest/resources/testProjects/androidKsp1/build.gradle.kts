plugins {
    id("com.android.application") version "8.13.0"
    id("io.github.schwarzit.lightsaber")
    kotlin("android") version "2.2.20"
    id("com.google.devtools.ksp") version "2.2.20-2.0.4"
}

@OptIn(com.google.devtools.ksp.KspExperimental::class)
ksp {
    useKsp2.set(false)
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
    implementation("com.google.dagger:dagger:2.57.2")
    ksp("com.google.dagger:dagger-compiler:2.57.2")
}
