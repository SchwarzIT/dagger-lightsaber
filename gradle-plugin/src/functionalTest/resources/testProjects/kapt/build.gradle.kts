import schwarz.it.lightsaber.gradle.Severity

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("kapt") version "1.9.20"
    id("schwarz.it.lightsaber")
}

lightsaber {
    unusedBindsAndProvides = Severity.Error
}

dependencies {
    implementation("com.google.dagger:dagger:2.48.1")
    kapt("com.google.dagger:dagger-compiler:2.48.1")
}
