package es.lidlplus.libs.lightsaber.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

class LightSaberPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply()
    }
}

private fun Project.apply() {
    val extension = extensions.create("lightsaber", LightSaberPluginExtension::class.java)

    pluginManager.withPlugin("kotlin-kapt") {
        dependencies.add("kapt", "es.lidlplus.lightsaber:lightsaber:0.0.1")
        extensions.configure(KaptExtension::class.java) {
            it.arguments {
                arg(
                    "LightSaber.UnusedBindInstance",
                    extension.unusedBindInstance.convention("error").get(),
                )
                arg(
                    "LightSaber.UnusedBindsAndProvides",
                    extension.unusedBindsAndProvides.convention("error").get(),
                )
                arg(
                    "LightSaber.UnusedDependencies",
                    extension.unusedDependencies.convention("error").get(),
                )
                arg("LightSaber.UnusedModules", extension.unusedModules.convention("error").get())
            }
        }
    }
}

interface LightSaberPluginExtension {
    val unusedBindInstance: Property<String>
    val unusedBindsAndProvides: Property<String>
    val unusedDependencies: Property<String>
    val unusedModules: Property<String>
}
