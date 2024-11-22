package schwarz.it.lightsaber.gradle.processors

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import schwarz.it.lightsaber.gradle.LightsaberExtension
import schwarz.it.lightsaber.gradle.LightsaberTask
import schwarz.it.lightsaber.gradle.capitalized
import schwarz.it.lightsaber.gradle.getArguments
import schwarz.it.lightsaber.gradle.lightsaberVersion
import schwarz.it.lightsaber.gradle.registerTask

internal fun Project.registerAnnotationProcessorTask(
    extension: LightsaberExtension,
    variant: String? = null,
): TaskProvider<LightsaberTask> {
    val variantName = variant?.capitalized()
    val lightsaberCheck = registerTask(extension, variantName.orEmpty())
    lightsaberCheck.configure { task ->
        val taskProvider = provider {
            if (variantName == null) {
                tasks.withType(JavaCompile::class.java)
            } else {
                tasks.withType(JavaCompile::class.java)
                    .matching { it.name.startsWith("compile$variantName") }
            }
        }
        task.dependsOn(taskProvider)

        task.source = taskProvider.get()
            .map { fileTree(it.destinationDirectory.dir("schwarz/it/lightsaber")).asFileTree }
            .reduce { acc, fileTree -> acc.plus(fileTree) }
            .matching { it.include("*.lightsaber") }
    }
    return lightsaberCheck
}

internal fun Project.configureLightsaberAnnotationProcessor(extension: LightsaberExtension) {
    dependencies.add("annotationProcessor", "io.github.schwarzit:lightsaber:$lightsaberVersion")
    tasks.withType(JavaCompile::class.java).configureEach {
        it.annotationProcessor {
            extension.getArguments().forEach { (key, value) -> arg(key, value) }
        }
    }
}

private fun JavaCompile.annotationProcessor(block: AnnotationProcessorScope.() -> Unit) {
    AnnotationProcessorScope(this).block()
}

private class AnnotationProcessorScope(val task: JavaCompile)

private fun AnnotationProcessorScope.arg(key: String, value: Any) {
    task.options.compilerArgs.add("-A$key=$value")
}
