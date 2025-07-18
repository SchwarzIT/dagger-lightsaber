plugins {
    id("com.android.application") version "8.11.1"
    id("io.github.schwarzit.lightsaber")
    kotlin("android") version "2.2.0"
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
}

@OptIn(com.google.devtools.ksp.KspExperimental::class)
ksp {
    useKsp2.set(true)
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
    implementation("com.google.dagger:dagger:2.56")
    ksp("com.google.dagger:dagger-compiler:2.56")
}
