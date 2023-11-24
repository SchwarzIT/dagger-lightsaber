plugins {
    kotlin("jvm") version "1.9.20"
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
    id("io.github.schwarzit.lightsaber")
}

dependencies {
    implementation("com.google.dagger:dagger:2.48.1")
    ksp("com.google.dagger:dagger-compiler:2.48.1")
}
