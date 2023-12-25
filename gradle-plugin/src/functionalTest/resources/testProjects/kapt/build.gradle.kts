plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("kapt") version "1.9.22"
    id("io.github.schwarzit.lightsaber")
}

dependencies {
    implementation("com.google.dagger:dagger:2.48.1")
    kapt("com.google.dagger:dagger-compiler:2.48.1")
}
