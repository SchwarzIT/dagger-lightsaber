plugins {
    kotlin("jvm") version "2.2.20"
    id("com.google.devtools.ksp") version "2.2.20-2.0.4"
    id("io.github.schwarzit.lightsaber")
}

@OptIn(com.google.devtools.ksp.KspExperimental::class)
ksp {
    useKsp2.set(true)
}

dependencies {
    implementation("com.google.dagger:dagger:2.57.1")
    ksp("com.google.dagger:dagger-compiler:2.57.1")
}
