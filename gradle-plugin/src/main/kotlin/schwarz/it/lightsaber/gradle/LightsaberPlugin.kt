package schwarz.it.lightsaber.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.tasks.BaseKapt

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
                arg("Lightsaber.CheckUnusedBindInstance", extension.unusedBindInstance.map(Severity::toKapt).get())
                arg("Lightsaber.CheckUnusedBindsAndProvides", extension.unusedBindsAndProvides.map(Severity::toKapt).get())
                arg("Lightsaber.CheckUnusedDependencies", extension.unusedDependencies.map(Severity::toKapt).get())
                arg("Lightsaber.CheckUnusedModules", extension.unusedModules.map(Severity::toKapt).get())
            }
        }
    }

    val lightsaberCheck = tasks.register("lightsaberCheck", LightsaberTask::class.java) { task ->
        pluginManager.withPlugin("kotlin-kapt") {
            task.dependsOn(provider { tasks.withType(BaseKapt::class.java) })
        }

        task.source = tasks.withType(BaseKapt::class.java)
            .map { fileTree(it.destinationDir.dir("schwarz/it/lightsaber")).asFileTree }
            .reduce { acc, fileTree -> acc.plus(fileTree) }
            .matching { it.include("*.lightsaber") }

        task.severities.set(
            objects.mapProperty(Rule::class.java, Severity::class.java).apply {
                Rule.entries.forEach { rule ->
                    put(
                        rule,
                        when (rule) {
                            Rule.UnusedBindInstance -> extension.unusedBindInstance
                            Rule.UnusedBindsAndProvides -> extension.unusedBindsAndProvides
                            Rule.UnusedDependencies -> extension.unusedDependencies
                            Rule.UnusedModules -> extension.unusedModules
                        },
                    )
                }
            },
        )
    }

    tasks.named("check").configure { it.dependsOn(lightsaberCheck) }
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
    Severity.Error -> true
    Severity.Warning -> true
    Severity.Ignore -> false
}
