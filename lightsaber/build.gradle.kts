import com.vanniktech.maven.publish.SonatypeHost.Companion.S01

plugins {
    id("kotlin-kapt")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.spotless)
}

kotlin {
    jvmToolchain(11)
}

tasks.test {
    useJUnitPlatform()
}

// from https://www.zacsweers.dev/kapts-hidden-test-costs/
tasks
    .matching { it.name.startsWith("kapt") && it.name.endsWith("TestKotlin") }
    .configureEach { enabled = false }

group = "io.github.schwarzit"
version = properties["version"]!!

dependencies {
    compileOnly(libs.dagger.spi)
    compileOnly(libs.google.auto.service)
    kapt(libs.google.auto.service)
    compileOnly(libs.ksp.api)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.dagger.compiler)
    testImplementation(libs.truth)
    testImplementation(libs.kctfork.core)
    testImplementation(libs.kctfork.ksp)
    testImplementation(libs.anvil.annotations)
    testImplementation(libs.anvil.compiler)
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        ktlint(libs.ktlint.get().version)
            .editorConfigOverride(
                mapOf(
                    "ktlint_standard_filename" to "disabled",
                    "ktlint_standard_class-naming" to "disabled",
                    "ktlint_standard_function-naming" to "disabled",
                    "ktlint_standard_property-naming" to "disabled",
                    "ktlint_standard_discouraged-comment-location" to "disabled",
                    "ktlint_standard_no-empty-file" to "disabled",
                    "ktlint_standard_function-expression-body" to "disabled",
                )
            )
    }
}

mavenPublishing {
    publishToMavenCentral(S01)
    coordinates(group.toString(), "lightsaber", version.toString())

    pom {
        name.set("Lightsaber")
        description.set("A Dagger 2 plugin to find unused dependencies declared in your Modules and Components.")
        inceptionYear.set("2023")
        url.set("https://github.com/SchwarzIT/dagger-lightsaber")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                name.set("Jenifer Arias Gallego")
                url.set("https://github.com/jenni-arias")
            }
            developer {
                name.set("Javier Luque Sanabria")
                url.set("https://github.com/javils")
            }
            developer {
                name.set("Raul Moreno Garcia")
                url.set("https://github.com/raul19")
            }
            developer {
                name.set("Brais Gabín")
                url.set("https://github.com/braisgabin")
            }
            developer {
                name.set("Alvaro Girona Arias")
                url.set("https://github.com/alvarogirona")
            }
        }
        scm {
            url.set("https://github.com/SchwarzIT/dagger-lightsaber")
            connection.set("scm:git:git://github.com/SchwarzIT/dagger-lightsaber.git")
            developerConnection.set("scm:git:ssh://git@github.com/SchwarzIT/dagger-lightsaber.git")
        }
    }
}
