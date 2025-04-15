package schwarz.it.lightsaber.gradle.processors

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.tasks.BaseKapt
import schwarz.it.lightsaber.gradle.LightsaberExtension
import schwarz.it.lightsaber.gradle.LightsaberTask
import schwarz.it.lightsaber.gradle.capitalized
import schwarz.it.lightsaber.gradle.getArguments
import schwarz.it.lightsaber.gradle.lightsaberVersion
import schwarz.it.lightsaber.gradle.registerTask

internal fun Project.registerKaptTask(
    extension: LightsaberExtension,
    variant: String? = null,
): TaskProvider<LightsaberTask> {
    val variantName = variant?.capitalized()
    val lightsaberCheck = registerTask(extension, variantName.orEmpty())
    lightsaberCheck.configure { task ->
        val lightsaberOutputDir = lightsaberOutputDir("kapt")
        val taskProvider = provider {
            tasks.withType(BaseKapt::class.java)
                .matching { it.name.startsWith("kapt${variantName.orEmpty()}") }
                .apply {
                    configureEach { kaptTask ->
                        val sourceSet = kaptTask.name
                            .removePrefix("kapt")
                            .removeSuffix("Kotlin")
                            .ifEmpty { "main" }
                            .replaceFirstChar { it.lowercaseChar() }
                        val output = lightsaberOutputDir.map { it.dir(sourceSet) }
                        kaptTask.annotationProcessorOptionProviders
                            .add(listOf(LightsaberArgumentProvider(output)))

                        kaptTask.outputs.dir(output)
                    }
                }
        }
        task.dependsOn(taskProvider)

        task.source += fileTree(lightsaberOutputDir)
    }
    return lightsaberCheck
}

internal fun Project.configureLightsaberKapt(extension: LightsaberExtension) {
    dependencies.add("kapt", "io.github.schwarzit:lightsaber:$lightsaberVersion")
    extensions.configure(KaptExtension::class.java) {
        it.arguments {
            extension.getArguments().forEach { (key, value) -> arg(key, value.toString()) }
        }
    }
}
