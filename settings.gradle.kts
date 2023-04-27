rootProject.name = "lightsaber"
include("gradle-plugin")
include("lightsaber")

dependencyResolutionManagement {

    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}
