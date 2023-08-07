package schwarz.it.lightsaber.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

class LightsaberPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply()
    }
}

private fun Project.apply() {
    val extension = extensions.create("lightsaber", LightsaberExtension::class.java).apply {
        unusedBindInstance.convention(Severity.Error)
        unusedBindsAndProvides.convention(Severity.Error)
        unusedDependencies.convention(Severity.Error)
        unusedModules.convention(Severity.Error)
    }

    pluginManager.withPlugin("kotlin-kapt") {
        dependencies.add("kapt", "schwarz.it.lightsaber:lightsaber:$lightsaberVersion")
        extensions.configure(KaptExtension::class.java) {
            it.arguments {
                arg("Lightsaber.UnusedBindInstance", extension.unusedBindInstance.map(Severity::toKapt).get())
                arg("Lightsaber.UnusedBindsAndProvides", extension.unusedBindsAndProvides.map(Severity::toKapt).get())
                arg("Lightsaber.UnusedDependencies", extension.unusedDependencies.map(Severity::toKapt).get())
                arg("Lightsaber.UnusedModules", extension.unusedModules.map(Severity::toKapt).get())
            }
        }
    }
}

interface LightsaberExtension {
    val unusedBindInstance: Property<Severity>
    val unusedBindsAndProvides: Property<Severity>
    val unusedDependencies: Property<Severity>
    val unusedModules: Property<Severity>
}

enum class Severity {
    Error,
    Warning,
    Ignore,
}

private fun Severity.toKapt() = when (this) {
    Severity.Error -> "error"
    Severity.Warning -> "warning"
    Severity.Ignore -> "ignore"
}
