package schwarz.it.lightsaber.gradle

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspTaskJvm
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.tasks.BaseKapt

class LightsaberPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply()
    }
}

private fun Project.apply() {
    val extension = extensions.create("lightsaber", LightsaberExtension::class.java).apply {
        emptyComponent.convention(Severity.Error)
        unusedBindInstance.convention(Severity.Error)
        unusedBindsAndProvides.convention(Severity.Error)
        unusedDependencies.convention(Severity.Error)
        unusedModules.convention(Severity.Error)
    }

    pluginManager.withPlugin("com.google.devtools.ksp") {
        dependencies.add("ksp", "schwarz.it.lightsaber:lightsaber:$lightsaberVersion")
        extensions.configure(KspExtension::class.java) {
            it.arg("Lightsaber.CheckEmptyComponent", extension.emptyComponent.toProcessor().get().toString())
            it.arg("Lightsaber.CheckUnusedBindInstance", extension.unusedBindInstance.toProcessor().get().toString())
            it.arg("Lightsaber.CheckUnusedBindsAndProvides", extension.unusedBindsAndProvides.toProcessor().get().toString())
            it.arg("Lightsaber.CheckUnusedDependencies", extension.unusedDependencies.toProcessor().get().toString())
            it.arg("Lightsaber.CheckUnusedModules", extension.unusedModules.toProcessor().get().toString())
        }

        val lightsaberCheck = registerTask(extension)
        lightsaberCheck.configure { task ->
            val taskProvider = provider { tasks.withType(KspTaskJvm::class.java) }
            task.dependsOn(taskProvider)

            task.source = taskProvider.get()
                .map { fileTree(it.destination.get().resolve("resources/schwarz/it/lightsaber")).asFileTree }
                .reduce { acc, fileTree -> acc.plus(fileTree) }
                .matching { it.include("*.lightsaber") }
        }

        tasks.named("check").configure { it.dependsOn(lightsaberCheck) }
    }

    pluginManager.withPlugin("kotlin-kapt") {
        dependencies.add("kapt", "schwarz.it.lightsaber:lightsaber:$lightsaberVersion")
        extensions.configure(KaptExtension::class.java) {
            it.arguments {
                arg("Lightsaber.CheckEmptyComponent", extension.emptyComponent.toProcessor().get())
                arg("Lightsaber.CheckUnusedBindInstance", extension.unusedBindInstance.toProcessor().get())
                arg("Lightsaber.CheckUnusedBindsAndProvides", extension.unusedBindsAndProvides.toProcessor().get())
                arg("Lightsaber.CheckUnusedDependencies", extension.unusedDependencies.toProcessor().get())
                arg("Lightsaber.CheckUnusedModules", extension.unusedModules.toProcessor().get())
            }
        }

        val lightsaberCheck = registerTask(extension)
        lightsaberCheck.configure { task ->
            val taskProvider = provider { tasks.withType(BaseKapt::class.java) }
            task.dependsOn(taskProvider)

            task.source = taskProvider.get()
                .map { fileTree(it.classesDir.dir("schwarz/it/lightsaber")).asFileTree }
                .reduce { acc, fileTree -> acc.plus(fileTree) }
                .matching { it.include("*.lightsaber") }
        }

        tasks.named("check").configure { it.dependsOn(lightsaberCheck) }
    }
}

private fun Project.registerTask(extension: LightsaberExtension): TaskProvider<LightsaberTask> {
    return tasks.register("lightsaberCheck", LightsaberTask::class.java) { task ->
        task.severities.set(
            objects.mapProperty(Rule::class.java, Severity::class.java).apply {
                Rule.entries.forEach { rule -> put(rule, rule.toPropertySeverity(extension)) }
            },
        )
    }
}

private fun Rule.toPropertySeverity(extension: LightsaberExtension): Property<Severity> {
    return when (this) {
        Rule.EmptyComponent -> extension.emptyComponent
        Rule.UnusedBindInstance -> extension.unusedBindInstance
        Rule.UnusedBindsAndProvides -> extension.unusedBindsAndProvides
        Rule.UnusedDependencies -> extension.unusedDependencies
        Rule.UnusedModules -> extension.unusedModules
    }
}

interface LightsaberExtension {
    val emptyComponent: Property<Severity>
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

private fun Property<Severity>.toProcessor(): Provider<Boolean> {
    return map { severity: Severity ->
        when (severity) {
            Severity.Error -> true
            Severity.Warning -> true
            Severity.Ignore -> false
        }
    }
}
