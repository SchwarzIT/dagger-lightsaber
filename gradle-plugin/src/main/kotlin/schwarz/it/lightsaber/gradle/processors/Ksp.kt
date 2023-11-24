package schwarz.it.lightsaber.gradle.processors

import com.android.build.api.variant.Variant
import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspTaskJvm
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import schwarz.it.lightsaber.gradle.LightsaberExtension
import schwarz.it.lightsaber.gradle.LightsaberTask
import schwarz.it.lightsaber.gradle.getArguments
import schwarz.it.lightsaber.gradle.lightsaberVersion
import schwarz.it.lightsaber.gradle.registerTask

fun Project.applyKsp(extension: LightsaberExtension) {
    val lightsaberCheck = registerKspTask(extension)
    tasks.named("check").configure { it.dependsOn(lightsaberCheck) }
}

internal fun Project.registerKspTask(
    extension: LightsaberExtension,
    variant: Variant? = null,
): TaskProvider<LightsaberTask> {
    val variantName = variant?.name?.capitalized()
    val lightsaberCheck = registerTask(extension, variantName.orEmpty())
    lightsaberCheck.configure { task ->
        val taskProvider = provider {
            if (variantName == null) {
                tasks.withType(KspTaskJvm::class.java)
            } else {
                tasks.withType(KspTaskJvm::class.java)
                    .matching { it.name.startsWith("ksp$variantName") }
            }
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
