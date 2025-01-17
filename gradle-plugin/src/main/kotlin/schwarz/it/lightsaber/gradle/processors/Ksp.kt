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
        val taskProvider = provider {
            tasks.withType(KspTaskJvm::class.java)
                .matching { it.name.startsWith("ksp${variantName.orEmpty()}") }
        }
        task.dependsOn(taskProvider)

        task.source = taskProvider.get()
            .map { fileTree(it.destination.get().resolve("resources/schwarz/it/lightsaber")).asFileTree }
            .reduce { acc, fileTree -> acc.plus(fileTree) }
            .matching { it.include("*.lightsaber") }
    }
    return lightsaberCheck
}

internal fun Project.configureLightsaberKsp(extension: LightsaberExtension) {
    dependencies.add("ksp", "io.github.schwarzit:lightsaber:$lightsaberVersion")
    extensions.configure(KspExtension::class.java) {
        extension.getArguments().forEach { (key, value) -> it.arg(key, value.toString()) }
    }
}
