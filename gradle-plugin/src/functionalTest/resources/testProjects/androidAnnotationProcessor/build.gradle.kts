plugins {
    id("com.android.application") version "8.7.2"
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
    implementation("com.google.dagger:dagger:2.53.1")
    annotationProcessor("com.google.dagger:dagger-compiler:2.53.1")
}
