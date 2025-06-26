plugins {
    kotlin("jvm") version "2.2.0"
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
    id("io.github.schwarzit.lightsaber")
}

@OptIn(com.google.devtools.ksp.KspExperimental::class)
ksp {
    useKsp2.set(false)
}

dependencies {
    implementation("com.google.dagger:dagger:2.53")
    ksp("com.google.dagger:dagger-compiler:2.53")
}
