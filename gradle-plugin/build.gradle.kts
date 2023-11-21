@file:Suppress("UnstableApiUsage")

import com.vanniktech.maven.publish.SonatypeHost.Companion.S01
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.spotless)
    id("java-gradle-plugin")
}

kotlin {
    jvmToolchain(libs.versions.jdk.version.get().toInt())
}

group = "io.github.schwarzit"
version = properties["version"]!!

testing {
    suites {
        getByName("test", JvmTestSuite::class) {
            useJUnitJupiter(libs.versions.junit.jupiter.get())

            dependencies {
                implementation(libs.junit.jupiter.api)
                runtimeOnly(libs.junit.jupiter.engine)
                implementation(libs.truth)
                implementation(libs.symbol.processing.gradle.plugin)
                implementation(libs.kotlin.gradle.plugin)
                implementation(gradleKotlinDsl())
            }
        }
        register("functionalTest", JvmTestSuite::class) {
            useJUnitJupiter(libs.versions.junit.jupiter.get())

            dependencies {
                implementation(libs.junit.jupiter.api)
                runtimeOnly(libs.junit.jupiter.engine)
                implementation(libs.truth)
            }
        }
    }
}

gradlePlugin {
    vcsUrl = "https://github.com/SchwarzIT/dagger-lightsaber"
    plugins {
        create("LightsaberPlugin") {
            id = "io.github.schwarzit.lightsaber"
            implementationClass = "schwarz.it.lightsaber.gradle.LightsaberPlugin"
        }
    }
    testSourceSets(sourceSets["functionalTest"])
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        ktlint(libs.ktlint.get().version)
    }
}

val testKitRuntimeOnly: Configuration by configurations.creating

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.symbol.processing.gradle.plugin)

    testKitRuntimeOnly(libs.kotlin.gradle.plugin)
    testKitRuntimeOnly(libs.symbol.processing.gradle.plugin)
}

// Manually inject dependency to gradle-testkit since the default injected plugin classpath is from `main.runtime`.
tasks.pluginUnderTestMetadata {
    pluginClasspath.from(testKitRuntimeOnly)
}

tasks.named("functionalTest") {
    dependsOn(provider { rootProject.project(":lightsaber").tasks.named("publishToMavenLocal") })
}

private val createVersionsKtFile by tasks.registering(Task::class) {
    inputs.property("version", properties["version"]!!)
    outputs.dir(layout.buildDirectory.dir("generated"))
    val versionKt = layout.buildDirectory.file("generated/Version.kt")
    doLast {
        versionKt.get().asFile.apply {
            ensureParentDirsCreated()
            writeText(
                """
                    package schwarz.it.lightsaber.gradle
                    
                    const val lightsaberVersion = "${inputs.properties["version"]}"
                    
                """.trimIndent(),
            )
        }
    }
}

sourceSets {
    main {
        kotlin {
            srcDir(createVersionsKtFile)
        }
    }
}

mavenPublishing {
    publishToMavenCentral(S01)
    coordinates(group.toString(), "lightsaber-gradle-plugin", version.toString())

    pom {
        name.set("Lightsaber Gradle Plugin")
        description.set("A Gradle plugin to make it easy to configure Lightsaber")
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
