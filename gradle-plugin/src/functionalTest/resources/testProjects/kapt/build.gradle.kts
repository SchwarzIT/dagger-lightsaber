plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("kapt") version "2.0.0"
    id("io.github.schwarzit.lightsaber")
}

dependencies {
    implementation("com.google.dagger:dagger:2.48.1")
    kapt("com.google.dagger:dagger-compiler:2.48.1")
}
