package schwarz.it.lightsaber.gradle.utils

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import schwarz.it.lightsaber.gradle.LightsaberExtension
import schwarz.it.lightsaber.gradle.Severity
import schwarz.it.lightsaber.gradle.lightsaberVersion
import schwarz.it.lightsaber.gradle.utils.AnnotationProcessor.Javac
import schwarz.it.lightsaber.gradle.utils.AnnotationProcessor.Kapt

internal fun Project.configureProcessor(extension: LightsaberExtension) {
    pluginManager.withPlugin("com.android.application") { configureLightsaberIfDagger(Javac, extension) }
    pluginManager.withPlugin("com.android.library") { configureLightsaberIfDagger(Javac, extension) }
    pluginManager.withPlugin("java") { configureLightsaberIfDagger(Javac, extension) }
    pluginManager.withPlugin("java-library") { configureLightsaberIfDagger(Javac, extension) }
    pluginManager.withPlugin("kotlin-kapt") { configureLightsaberIfDagger(Kapt, extension) }
}

private fun Project.configureLightsaberIfDagger(type: AnnotationProcessor, extension: LightsaberExtension) {
    configurations.findByName(type.configurationName)?.withDependencies {
        if (it.any(Dependency::isDaggerCompiler)) {
            configureLightsaber(type, extension)
        }
    }
}

private fun Project.configureLightsaber(type: AnnotationProcessor, extension: LightsaberExtension) {
    dependencies.add(type.configurationName, "schwarz.it.lightsaber:lightsaber:$lightsaberVersion")

    when (type) {
        Javac -> configureLightsaberJavac(extension)
        Kapt -> configureLightsaberKapt(extension)
    }
}

private fun Project.configureLightsaberJavac(extension: LightsaberExtension) {
    tasks.withType(JavaCompile::class.java) {
        it.options.compilerArgs.addAll(
            listOf(
                "-ALightsaber.CheckUnusedBindInstance=${extension.unusedBindInstance.toProcessor().get()}",
                "-ALightsaber.CheckUnusedBindsAndProvides=${extension.unusedBindsAndProvides.toProcessor().get()}",
                "-ALightsaber.CheckUnusedDependencies=${extension.unusedDependencies.toProcessor().get()}",
                "-ALightsaber.CheckUnusedModules=${extension.unusedModules.toProcessor().get()}",
            ),
        )
    }
}

private fun Project.configureLightsaberKapt(extension: LightsaberExtension) {
    extensions.configure(KaptExtension::class.java) {
        it.arguments {
            arg("Lightsaber.CheckUnusedBindInstance", extension.unusedBindInstance.toProcessor().get())
            arg("Lightsaber.CheckUnusedBindsAndProvides", extension.unusedBindsAndProvides.toProcessor().get())
            arg("Lightsaber.CheckUnusedDependencies", extension.unusedDependencies.toProcessor().get())
            arg("Lightsaber.CheckUnusedModules", extension.unusedModules.toProcessor().get())
        }
    }
}

private fun Dependency.isDaggerCompiler(): Boolean = group == "com.google.dagger"

private enum class AnnotationProcessor { Javac, Kapt }

private val AnnotationProcessor.configurationName: String
    get() = when (this) {
        Javac -> "annotationProcessor"
        Kapt -> "kapt"
    }

private fun Property<Severity>.toProcessor(): Provider<Boolean> {
    return map { severity: Severity ->
        when (severity) {
            Severity.Error -> true
            Severity.Warning -> true
            Severity.Ignore -> false
        }
    }
}
