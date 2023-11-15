import schwarz.it.lightsaber.gradle.Severity

plugins {
    `java`
    id("io.github.schwarzit.lightsaber")
}

lightsaber {
    unusedBindsAndProvides = Severity.Error
}

dependencies {
    implementation("com.google.dagger:dagger:2.48.1")
    annotationProcessor("com.google.dagger:dagger-compiler:2.48.1")
}
