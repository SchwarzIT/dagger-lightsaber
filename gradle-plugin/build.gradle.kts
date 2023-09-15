import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("com.vanniktech.maven.publish")
}

kotlin {
    jvmToolchain(11)
}

tasks.test {
    useJUnitPlatform()
}

group = "schwarz.it.lightsaber"
version = properties["version"]!!

gradlePlugin {
    plugins {
        create("LightsaberPlugin") {
            id = "schwarz.it.lightsaber"
            implementationClass = "schwarz.it.lightsaber.gradle.LightsaberPlugin"
        }
    }
}

dependencies {
    implementation(project(":lightsaber"))

    compileOnly(gradleApi())
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
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
                    
                """.trimIndent()
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
        scm {
            url.set("https://github.com/SchwarzIT/dagger-lightsaber")
            connection.set("scm:git:git://github.com/SchwarzIT/dagger-lightsaber.git")
            developerConnection.set("scm:git:ssh://git@github.com/SchwarzIT/dagger-lightsaber.git")
        }
    }
}
