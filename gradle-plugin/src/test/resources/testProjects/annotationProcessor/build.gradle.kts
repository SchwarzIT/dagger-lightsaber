import schwarz.it.lightsaber.gradle.Severity

plugins {
    id("java")
    id("schwarz.it.lightsaber")
}

tasks.test {
    useJUnitPlatform()
}

lightsaber {
    unusedBindsAndProvides = Severity.Error
}

dependencies {
    implementation("com.google.dagger:dagger:2.48")
    annotationProcessor("com.google.dagger:dagger-compiler:2.48")
}
