package schwarz.it.lightsaber.gradle.processors

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

enum class Processor { AnnotationProcessor, Kapt, Ksp }

internal fun Project.withDaggerCompiler(block: Project.(Processor) -> Unit) {
    withDaggerCompiler("annotationProcessor") { block(Processor.AnnotationProcessor) }
    withDaggerCompiler("kapt") { block(Processor.Kapt) }
    withDaggerCompiler("ksp") { block(Processor.Ksp) }
}

private fun Project.withDaggerCompiler(configurationName: String, block: Project.() -> Unit) {
    afterEvaluate {
        if (configurations.findByName(configurationName)?.dependencies.orEmpty().any { it.isDaggerCompiler() }) {
            block()
        }
    }
}

private fun Dependency.isDaggerCompiler(): Boolean {
    return group == "com.google.dagger" && name == "dagger-compiler"
}
