package schwarz.it.lightsaber.gradle

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspTaskJvm
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.tasks.BaseKapt

class LightsaberPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.apply()
    }
}

private fun Project.apply() {
    val extension = extensions.create("lightsaber", LightsaberExtension::class.java).apply {
        emptyComponent.convention(Severity.Error)
        unusedBindInstance.convention(Severity.Error)
        unusedBindsAndProvides.convention(Severity.Error)
        unusedDependencies.convention(Severity.Error)
        unusedMembersInjectionMethods.convention(Severity.Error)
        unusedModules.convention(Severity.Error)
    }

    pluginManager.withPlugin("com.google.devtools.ksp") { _ ->
        afterEvaluate {
            if (configurations.getByName("ksp").dependencies.any { it.isDaggerCompiler() }) {
                applyKsp(extension)
            }
        }
    }

    pluginManager.withPlugin("kotlin-kapt") { _ ->
        afterEvaluate {
            if (configurations.getByName("kapt").dependencies.any { it.isDaggerCompiler() }) {
                applyKapt(extension)
            }
        }
    }

    pluginManager.withPlugin("java") { _ ->
        afterEvaluate {
            if (configurations.getByName("annotationProcessor").dependencies.any { it.isDaggerCompiler() }) {
                applyAnnotationProcessor(extension)
            }
        }
    }
}

fun Project.applyKsp(extension: LightsaberExtension) {
    dependencies.add("ksp", "io.github.schwarzit:lightsaber:$lightsaberVersion")
    extensions.configure(KspExtension::class.java) {
        extension.getArguments().forEach { (key, value) -> it.arg(key, value.toString()) }
    }

    val lightsaberCheck = registerTask(extension)
    lightsaberCheck.configure { task ->
        val taskProvider = provider { tasks.withType(KspTaskJvm::class.java) }
        task.dependsOn(taskProvider)

        task.source = taskProvider.get()
            .map { fileTree(it.destination.get().resolve("resources/schwarz/it/lightsaber")).asFileTree }
            .reduce { acc, fileTree -> acc.plus(fileTree) }
            .matching { it.include("*.lightsaber") }
    }

    tasks.named("check").configure { it.dependsOn(lightsaberCheck) }
}

fun Project.applyKapt(extension: LightsaberExtension) {
    dependencies.add("kapt", "io.github.schwarzit:lightsaber:$lightsaberVersion")
    extensions.configure(KaptExtension::class.java) {
        it.arguments {
            extension.getArguments().forEach {(key, value) -> arg(key, value) }
        }
    }

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

fun Project.applyAnnotationProcessor(extension: LightsaberExtension) {
    dependencies.add("annotationProcessor", "io.github.schwarzit:lightsaber:$lightsaberVersion")
    tasks.withType(JavaCompile::class.java).configureEach {
        it.annotationProcessor {
            extension.getArguments().forEach { (key, value) -> arg(key, value) }
        }
    }

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

private fun Project.registerTask(extension: LightsaberExtension): TaskProvider<LightsaberTask> {
    return tasks.register("lightsaberCheck", LightsaberTask::class.java) { task ->
        task.severities.set(
            objects.mapProperty(Rule::class.java, Severity::class.java).apply {
                Rule.entries.forEach { rule -> put(rule, rule.toPropertySeverity(extension)) }
            },
        )
    }
}

private fun Rule.toPropertySeverity(extension: LightsaberExtension): Property<Severity> {
    return when (this) {
        Rule.EmptyComponent -> extension.emptyComponent
        Rule.UnusedBindInstance -> extension.unusedBindInstance
        Rule.UnusedBindsAndProvides -> extension.unusedBindsAndProvides
        Rule.UnusedDependencies -> extension.unusedDependencies
        Rule.UnusedMembersInjectionMethods -> extension.unusedMembersInjectionMethods
        Rule.UnusedModules -> extension.unusedModules
    }
}

interface LightsaberExtension {
    val emptyComponent: Property<Severity>
    val unusedBindInstance: Property<Severity>
    val unusedBindsAndProvides: Property<Severity>
    val unusedDependencies: Property<Severity>
    val unusedMembersInjectionMethods: Property<Severity>
    val unusedModules: Property<Severity>
}

enum class Severity {
    Error,
    Warning,
    Ignore,
}

private fun LightsaberExtension.getArguments() = mapOf(
    "Lightsaber.CheckEmptyComponent" to emptyComponent.toArgument(),
    "Lightsaber.CheckUnusedBindInstance" to unusedBindInstance.toArgument(),
    "Lightsaber.CheckUnusedBindsAndProvides" to unusedBindsAndProvides.toArgument(),
    "Lightsaber.CheckUnusedDependencies" to unusedDependencies.toArgument(),
    "Lightsaber.CheckUnusedMembersInjectionMethods" to unusedMembersInjectionMethods.toArgument(),
    "Lightsaber.CheckUnusedModules" to unusedModules.toArgument(),
)

private fun Property<Severity>.toArgument(): Boolean {
    return map { severity: Severity ->
        when (severity) {
            Severity.Error -> true
            Severity.Warning -> true
            Severity.Ignore -> false
        }
    }.get()
}

private fun Dependency.isDaggerCompiler(): Boolean {
    return group == "com.google.dagger" && name == "dagger-compiler"
}

private fun JavaCompile.annotationProcessor(block: AnnotationProcessorScope.() -> Unit) {
    AnnotationProcessorScope(this).block()
}

private class AnnotationProcessorScope(val task: JavaCompile)

private fun AnnotationProcessorScope.arg(key: String, value: Any) {
    task.options.compilerArgs.add("-A$key=$value")
}
