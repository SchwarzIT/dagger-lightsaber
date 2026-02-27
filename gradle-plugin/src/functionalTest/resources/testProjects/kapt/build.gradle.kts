plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("kapt") version "2.3.10"
    id("io.github.schwarzit.lightsaber")
}

dependencies {
    implementation("com.google.dagger:dagger:2.59.2")
    kapt("com.google.dagger:dagger-compiler:2.59.2")
}
