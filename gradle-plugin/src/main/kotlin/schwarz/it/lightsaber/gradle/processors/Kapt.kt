package schwarz.it.lightsaber.gradle.processors

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.tasks.BaseKapt
import schwarz.it.lightsaber.gradle.LightsaberExtension
import schwarz.it.lightsaber.gradle.getArguments
import schwarz.it.lightsaber.gradle.lightsaberVersion
import schwarz.it.lightsaber.gradle.registerTask

fun Project.applyKapt(extension: LightsaberExtension) {
    configureLightsaberKapt(extension)

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

private fun Project.configureLightsaberKapt(extension: LightsaberExtension) {
    dependencies.add("kapt", "io.github.schwarzit:lightsaber:$lightsaberVersion")
    extensions.configure(KaptExtension::class.java) {
        it.arguments {
            extension.getArguments().forEach { (key, value) -> arg(key, value) }
        }
    }
}
