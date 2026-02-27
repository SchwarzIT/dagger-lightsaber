plugins {
    id("com.android.application") version "9.0.1"
    id("io.github.schwarzit.lightsaber")
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
    annotationProcessor("com.google.dagger:dagger-compiler:2.59.2")
}
