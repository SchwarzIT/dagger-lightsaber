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
        enable.convention(true)
        unusedBindInstance.convention(Severity.Error)
        unusedBindsAndProvides.convention(Severity.Error)
        unusedDependencies.convention(Severity.Error)
        unusedModules.convention(Severity.Error)
    }

    pluginManager.withPlugin("kotlin-kapt") {
        dependencies.add("kapt", "schwarz.it.lightsaber:lightsaber:$lightsaberVersion")
        extensions.configure(KaptExtension::class.java) {
            it.arguments {
                fun Property<Severity>.getIfEnabled() = if (extension.enable.get()) {
                    this.get()
                } else {
                    Severity.Ignore
                }.toKapt()

                arg("Lightsaber.UnusedBindInstance", extension.unusedBindInstance.getIfEnabled())
                arg("Lightsaber.UnusedBindsAndProvides", extension.unusedBindsAndProvides.getIfEnabled())
                arg("Lightsaber.UnusedDependencies", extension.unusedDependencies.getIfEnabled())
                arg("Lightsaber.UnusedModules", extension.unusedModules.getIfEnabled())
            }
        }
    }
}

interface LightsaberExtension {
    val enable: Property<Boolean>
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
