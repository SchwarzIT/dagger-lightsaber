plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("kapt") version "2.2.20"
    id("io.github.schwarzit.lightsaber")
}

dependencies {
    implementation("com.google.dagger:dagger:2.59.1")
    kapt("com.google.dagger:dagger-compiler:2.59.1")
}
