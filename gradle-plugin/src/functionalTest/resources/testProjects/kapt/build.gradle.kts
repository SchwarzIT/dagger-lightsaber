plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("kapt") version "2.2.10"
    id("io.github.schwarzit.lightsaber")
}

dependencies {
    implementation("com.google.dagger:dagger:2.57.1")
    kapt("com.google.dagger:dagger-compiler:2.57.1")
}
