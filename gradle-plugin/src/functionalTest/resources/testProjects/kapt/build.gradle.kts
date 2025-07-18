plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("kapt") version "2.1.21"
    id("io.github.schwarzit.lightsaber")
}

dependencies {
    implementation("com.google.dagger:dagger:2.57")
    kapt("com.google.dagger:dagger-compiler:2.57")
}
