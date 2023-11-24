package schwarz.it.lightsaber.gradle.processors

import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import schwarz.it.lightsaber.gradle.LightsaberExtension
import schwarz.it.lightsaber.gradle.getArguments
import schwarz.it.lightsaber.gradle.lightsaberVersion
import schwarz.it.lightsaber.gradle.registerTask

fun Project.applyAnnotationProcessor(extension: LightsaberExtension) {
    val lightsaberCheck = registerTask(extension)
    lightsaberCheck.configure { task ->
        val taskProvider = provider { tasks.withType(JavaCompile::class.java) }
        task.dependsOn(taskProvider)

        task.source = taskProvider.get()
            .map { fileTree(it.destinationDirectory.dir("schwarz/it/lightsaber")).asFileTree }
            .reduce { acc, fileTree -> acc.plus(fileTree) }
            .matching { it.include("*.lightsaber") }
    }

    tasks.named("check").configure { it.dependsOn(lightsaberCheck) }
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
