import schwarz.it.lightsaber.gradle.Severity

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("kapt") version "1.9.10"
    id("schwarz.it.lightsaber")
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

lightsaber {
    unusedBindsAndProvides = Severity.Error
}

dependencies {
    implementation("com.google.dagger:dagger:2.48")
    kapt("com.google.dagger:dagger-compiler:2.48")
}
