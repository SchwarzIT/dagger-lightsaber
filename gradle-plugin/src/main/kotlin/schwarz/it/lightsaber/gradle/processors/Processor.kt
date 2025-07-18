package schwarz.it.lightsaber.gradle.processors

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.process.CommandLineArgumentProvider

enum class Processor { AnnotationProcessor, Kapt, Ksp }

internal fun Project.withDaggerCompiler(block: Project.(Processor) -> Unit) {
    withDaggerCompiler("annotationProcessor") { block(Processor.AnnotationProcessor) }
    withDaggerCompiler("kapt") { block(Processor.Kapt) }
    withDaggerCompiler("ksp") { block(Processor.Ksp) }
}

private fun Project.withDaggerCompiler(configurationName: String, block: Project.() -> Unit) {
    afterEvaluate {
        val daggerDependency = configurations.findByName(configurationName)?.dependencies.orEmpty()
            .find { it.isDaggerCompiler() }

        if (daggerDependency != null) {
            if (daggerDependency.isAtLeastVersion(major = MAJOR, minor = MINOR)) {
                block()
            } else {
                error("This version of lightsaber only supports dagger $MAJOR.$MINOR or greater")
            }
        }
    }
}

private fun Dependency.isDaggerCompiler(): Boolean {
    return group == "com.google.dagger" && name == "dagger-compiler"
}

private fun Dependency.isAtLeastVersion(major: Int, minor: Int): Boolean {
    val versionName = version ?: return true
    val (currentMajor, currentMinor) = versionName.split(".").mapNotNull { it.toIntOrNull() }

    return currentMajor > major || (currentMajor == major && currentMinor >= minor)
}

private const val MAJOR = 2
private const val MINOR = 53

internal fun Project.lightsaberOutputDir(tech: String) = layout.buildDirectory.dir("generated/lightsaber/$tech")

internal class LightsaberArgumentProvider(
    private val outputDir: Provider<Directory>,
    private val ksp: Boolean = false,
) : CommandLineArgumentProvider {
    override fun asArguments() = listOf("${if (ksp) "" else "-A"}Lightsaber.path=${outputDir.get().asFile.path}")
}
