import com.vanniktech.maven.publish.SonatypeHost.Companion.S01

plugins {
    id("kotlin")
    id("kotlin-kapt")
    id("com.vanniktech.maven.publish")
    id("com.diffplug.spotless")
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
    implementation("com.google.dagger:dagger-spi:2.48.1")
    compileOnly("com.google.auto.service:auto-service:1.1.1")
    kapt("com.google.auto.service:auto-service:1.1.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
    testImplementation("com.google.dagger:dagger-compiler:2.48.1")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("dev.zacsweers.kctfork:core:0.4.0")
    testImplementation("dev.zacsweers.kctfork:ksp:0.4.0")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        ktlint("0.48.2")
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
