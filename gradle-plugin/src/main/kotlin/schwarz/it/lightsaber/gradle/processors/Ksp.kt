package schwarz.it.lightsaber.gradle.processors

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspTaskJvm
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import schwarz.it.lightsaber.gradle.LightsaberExtension
import schwarz.it.lightsaber.gradle.LightsaberTask
import schwarz.it.lightsaber.gradle.capitalized
import schwarz.it.lightsaber.gradle.getArguments
import schwarz.it.lightsaber.gradle.lightsaberVersion
import schwarz.it.lightsaber.gradle.registerTask

internal fun Project.registerKspTask(
    extension: LightsaberExtension,
    variant: String? = null,
): TaskProvider<LightsaberTask> {
    val variantName = variant?.capitalized()
    val lightsaberCheck = registerTask(extension, variantName.orEmpty())
    lightsaberCheck.configure { task ->
        val lightsaberOutputDir = lightsaberOutputDir("ksp")
        val taskProvider = provider {
            tasks.withType(KspTaskJvm::class.java)
                .matching { it.name.startsWith("ksp${variantName.orEmpty()}") }
                .apply {
                    configureEach { kspTask ->
                        val sourceSet = kspTask.name
                            .removePrefix("ksp")
                            .removeSuffix("Kotlin")
                            .ifEmpty { "main" }
                            .replaceFirstChar { it.lowercaseChar() }
                        val output = lightsaberOutputDir.map { it.dir(sourceSet) }
                        kspTask.commandLineArgumentProviders.add(LightsaberArgumentProvider(output, ksp = true))

                        kspTask.outputs.dir(output)
                    }
                }
        }
        task.dependsOn(taskProvider)

        task.source += fileTree(lightsaberOutputDir)
    }
    return lightsaberCheck
}

internal fun Project.configureLightsaberKsp(extension: LightsaberExtension) {
    dependencies.add("ksp", "io.github.schwarzit:lightsaber:$lightsaberVersion")
    extensions.configure(KspExtension::class.java) {
        extension.getArguments().forEach { (key, value) -> it.arg(key, value.toString()) }
    }
}
