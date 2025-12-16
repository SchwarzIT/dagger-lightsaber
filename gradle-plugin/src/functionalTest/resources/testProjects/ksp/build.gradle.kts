plugins {
    kotlin("jvm") version "2.2.20"
    id("com.google.devtools.ksp") version "2.3.4"
    id("io.github.schwarzit.lightsaber")
}

dependencies {
    implementation("com.google.dagger:dagger:2.57.2")
    ksp("com.google.dagger:dagger-compiler:2.57.2")
}
