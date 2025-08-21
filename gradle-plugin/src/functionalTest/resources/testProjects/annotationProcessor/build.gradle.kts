plugins {
    `java`
    id("io.github.schwarzit.lightsaber")
}

dependencies {
    implementation("com.google.dagger:dagger:2.57.1")
    annotationProcessor("com.google.dagger:dagger-compiler:2.57.1")
}
