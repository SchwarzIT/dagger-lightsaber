plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("kapt") version "2.2.21"
    id("io.github.schwarzit.lightsaber")
}

dependencies {
    implementation("com.google.dagger:dagger:2.57.2")
    kapt("com.google.dagger:dagger-compiler:2.57.2")
}
