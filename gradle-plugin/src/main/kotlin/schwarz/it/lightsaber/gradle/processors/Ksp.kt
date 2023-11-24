package schwarz.it.lightsaber.gradle.processors

import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspTaskJvm
import org.gradle.api.Project
import schwarz.it.lightsaber.gradle.LightsaberExtension
import schwarz.it.lightsaber.gradle.getArguments
import schwarz.it.lightsaber.gradle.lightsaberVersion
import schwarz.it.lightsaber.gradle.registerTask

fun Project.applyKsp(extension: LightsaberExtension) {
    configureLightsaberKsp(extension)

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

internal fun Project.configureLightsaberKsp(extension: LightsaberExtension) {
    dependencies.add("ksp", "io.github.schwarzit:lightsaber:$lightsaberVersion")
    extensions.configure(KspExtension::class.java) {
        extension.getArguments().forEach { (key, value) -> it.arg(key, value.toString()) }
    }
}
