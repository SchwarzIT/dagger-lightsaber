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
        val taskProvider = provider {
            if (variant == null) {
                tasks.withType(BaseKapt::class.java)
            } else {
                tasks.withType(BaseKapt::class.java)
                    .matching { it.name.startsWith("kapt$variantName") }
            }
        }
        task.dependsOn(taskProvider)

        task.source = taskProvider.get()
            .map { fileTree(it.classesDir.dir("schwarz/it/lightsaber")).asFileTree }
            .reduce { acc, fileTree -> acc.plus(fileTree) }
            .matching { it.include("*.lightsaber") }
    }
    return lightsaberCheck
}

internal fun Project.configureLightsaberKapt(extension: LightsaberExtension) {
    dependencies.add("kapt", "io.github.schwarzit:lightsaber:$lightsaberVersion")
    extensions.configure(KaptExtension::class.java) {
        it.arguments {
            extension.getArguments().forEach { (key, value) -> arg(key, value) }
        }
    }
}
