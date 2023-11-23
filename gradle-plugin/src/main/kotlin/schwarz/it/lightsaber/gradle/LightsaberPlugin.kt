package schwarz.it.lightsaber.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import schwarz.it.lightsaber.gradle.processors.applyAnnotationProcessor
import schwarz.it.lightsaber.gradle.processors.applyKapt
import schwarz.it.lightsaber.gradle.processors.applyKsp

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
        unusedMembersInjectionMethods.convention(Severity.Error)
        unusedModules.convention(Severity.Error)
    }

    pluginManager.withPlugin("com.google.devtools.ksp") { _ ->
        afterEvaluate {
            if (configurations.getByName("ksp").dependencies.any { it.isDaggerCompiler() }) {
                applyKsp(extension)
            }
        }
    }

    pluginManager.withPlugin("kotlin-kapt") { _ ->
        afterEvaluate {
            if (configurations.getByName("kapt").dependencies.any { it.isDaggerCompiler() }) {
                applyKapt(extension)
            }
        }
    }

    pluginManager.withPlugin("java") { _ ->
        afterEvaluate {
            if (configurations.getByName("annotationProcessor").dependencies.any { it.isDaggerCompiler() }) {
                applyAnnotationProcessor(extension)
            }
        }
    }
}

internal fun Project.registerTask(extension: LightsaberExtension): TaskProvider<LightsaberTask> {
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
        Rule.UnusedMembersInjectionMethods -> extension.unusedMembersInjectionMethods
        Rule.UnusedModules -> extension.unusedModules
    }
}

private fun Dependency.isDaggerCompiler(): Boolean {
    return group == "com.google.dagger" && name == "dagger-compiler"
}
