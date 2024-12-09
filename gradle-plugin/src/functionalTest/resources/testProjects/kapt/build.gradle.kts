plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("kapt") version "1.9.25"
    id("io.github.schwarzit.lightsaber")
}

dependencies {
    implementation("com.google.dagger:dagger:2.53.1")
    kapt("com.google.dagger:dagger-compiler:2.53.1")
}
