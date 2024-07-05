package schwarz.it.lightsaber.gradle

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import schwarz.it.lightsaber.gradle.processors.Processor
import schwarz.it.lightsaber.gradle.processors.applyAndroidAnnotationProcessor
import schwarz.it.lightsaber.gradle.processors.configureLightsaberAnnotationProcessor
import schwarz.it.lightsaber.gradle.processors.configureLightsaberKapt
import schwarz.it.lightsaber.gradle.processors.configureLightsaberKsp
import schwarz.it.lightsaber.gradle.processors.registerAnnotationProcessorTask
import schwarz.it.lightsaber.gradle.processors.registerKaptTask
import schwarz.it.lightsaber.gradle.processors.registerKspTask
import schwarz.it.lightsaber.gradle.processors.withDaggerCompiler

class LightsaberPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply()
    }
}

private fun Project.apply() {
    val extension = extensions.create("lightsaber", LightsaberExtension::class.java).apply {
        emptyComponents.convention(Severity.Error)
        unusedBindsInstances.convention(Severity.Error)
        unusedBindsAndProvides.convention(Severity.Error)
        unusedDependencies.convention(Severity.Error)
        unusedInject.convention(Severity.Error)
        unusedMembersInjectionMethods.convention(Severity.Error)
        unusedModules.convention(Severity.Error)
        unusedScopes.convention(Severity.Error)
    }

    withDaggerCompiler { processor ->
        when (processor) {
            Processor.AnnotationProcessor -> configureLightsaberAnnotationProcessor(extension)
            Processor.Kapt -> configureLightsaberKapt(extension)
            Processor.Ksp -> configureLightsaberKsp(extension)
        }

        if (!androidPluginApplied()) {
            val lightsaberCheck = when (processor) {
                Processor.AnnotationProcessor -> registerAnnotationProcessorTask(extension)
                Processor.Kapt -> registerKaptTask(extension)
                Processor.Ksp -> registerKspTask(extension)
            }
            tasks.named("check").configure { it.dependsOn(lightsaberCheck) }
        }
    }

    if (androidPluginApplied()) {
        applyAndroidAnnotationProcessor(extension)
    }
}

private fun Project.androidPluginApplied(): Boolean {
    return extensions.findByType(BaseExtension::class.java) != null
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
        Rule.EmptyComponents -> extension.emptyComponents
        Rule.UnusedBindsInstances -> extension.unusedBindsInstances
        Rule.UnusedBindsAndProvides -> extension.unusedBindsAndProvides
        Rule.UnusedDependencies -> extension.unusedDependencies
        Rule.UnusedInject -> extension.unusedInject
        Rule.UnusedMembersInjectionMethods -> extension.unusedMembersInjectionMethods
        Rule.UnusedModules -> extension.unusedModules
        Rule.UnusedScopes -> extension.unusedScopes
    }
}
