import schwarz.it.lightsaber.gradle.Severity

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("kapt") version "1.9.23"
    id("io.github.schwarzit.lightsaber")
}

kotlin {
    jvmToolchain(11)
}

tasks.test {
    useJUnitPlatform()
}

lightsaber {
    unusedBindsAndProvides = Severity.Error
}

dependencies {
    implementation("com.google.dagger:dagger:2.51.1")
    kapt("com.google.dagger:dagger-compiler:2.51.1")
}
