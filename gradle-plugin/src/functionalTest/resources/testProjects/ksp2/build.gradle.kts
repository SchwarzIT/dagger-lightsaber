plugins {
    kotlin("jvm") version "2.1.21"
    id("com.google.devtools.ksp") version "2.1.21-2.0.2"
    id("io.github.schwarzit.lightsaber")
}

@OptIn(com.google.devtools.ksp.KspExperimental::class)
ksp {
    useKsp2.set(true)
}

dependencies {
    implementation("com.google.dagger:dagger:2.53")
    ksp("com.google.dagger:dagger-compiler:2.53")
}
