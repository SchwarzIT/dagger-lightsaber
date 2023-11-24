package schwarz.it.lightsaber.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import schwarz.it.lightsaber.gradle.processors.applyAndroidAnnotationProcessor
import schwarz.it.lightsaber.gradle.processors.applyAnnotationProcessor
import schwarz.it.lightsaber.gradle.processors.applyKapt
import schwarz.it.lightsaber.gradle.processors.applyKsp
import schwarz.it.lightsaber.gradle.processors.configureLightsaberAnnotationProcessor
import schwarz.it.lightsaber.gradle.processors.configureLightsaberKapt
import schwarz.it.lightsaber.gradle.processors.configureLightsaberKsp

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

    withDaggerCompiler("annotationProcessor") {
        configureLightsaberAnnotationProcessor(extension)
    }
    withDaggerCompiler("kapt") {
        configureLightsaberKapt(extension)
    }
    withDaggerCompiler("ksp") {
        configureLightsaberKsp(extension)
    }

    pluginManager.withPlugin("com.google.devtools.ksp") { _ ->
        withDaggerCompiler("ksp") {
            if (!pluginManager.hasPlugin("com.android.application")) {
                applyKsp(extension)
            }
        }
    }

    pluginManager.withPlugin("kotlin-kapt") { _ ->
        withDaggerCompiler("kapt") {
            if (!pluginManager.hasPlugin("com.android.application")) {
                applyKapt(extension)
            }
        }
    }

    pluginManager.withPlugin("java") { _ ->
        withDaggerCompiler("annotationProcessor") {
            applyAnnotationProcessor(extension)
        }
    }

    pluginManager.withPlugin("com.android.application") { _ ->
        applyAndroidAnnotationProcessor(extension)
    }
}

internal fun Project.registerTask(
    extension: LightsaberExtension,
    variantName: String = "",
): TaskProvider<LightsaberTask> {
    return tasks.register("lightsaber${variantName}Check", LightsaberTask::class.java) { task ->
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

internal fun Project.withDaggerCompiler(configurationName: String, block: Project.() -> Unit) {
    afterEvaluate {
        if (configurations.findByName(configurationName)?.dependencies.orEmpty().any { it.isDaggerCompiler() }) {
            block()
        }
    }
}

private fun Dependency.isDaggerCompiler(): Boolean {
    return group == "com.google.dagger" && name == "dagger-compiler"
}
