plugins {
    id("kotlin")
    id("kotlin-kapt")
    id("com.vanniktech.maven.publish")
    id("com.diffplug.spotless")
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// from https://www.zacsweers.dev/kapts-hidden-test-costs/
tasks
    .matching { it.name.startsWith("kapt") && it.name.endsWith("TestKotlin") }
    .configureEach { enabled = false }

group = "com.schwarzit.lightsaber"
version = "0.0.1"

dependencies {
    implementation("com.google.dagger:dagger-spi:2.46")
    compileOnly("com.google.auto.service:auto-service:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("com.google.dagger:dagger-compiler:2.46")
    testImplementation("com.google.testing.compile:compile-testing:0.21.0")
    testImplementation("com.google.truth:truth:1.1.3")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        ktlint("0.48.2")
    }
}

mavenPublishing {
    coordinates(group.toString(), "lightsaber", version.toString())

    pom {
        name.set("Lightsaber")
        description.set("A Dagger 2 plugin to find unused dependencies declared in your Modules and Components.")
        inceptionYear.set("2023")
        url.set("https://github.com/username/mylibrary/") // TODO
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("username")
                name.set("User Name")
                url.set("https://github.com/username/")
            }
        }
        scm { // TODO
            url.set("https://github.com/username/mylibrary/")
            connection.set("scm:git:git://github.com/username/mylibrary.git")
            developerConnection.set("scm:git:ssh://git@github.com/username/mylibrary.git")
        }
    }
}
