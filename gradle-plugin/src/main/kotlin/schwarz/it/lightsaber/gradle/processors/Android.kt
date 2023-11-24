package schwarz.it.lightsaber.gradle.processors

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.BuildType
import com.google.devtools.ksp.gradle.KspTaskJvm
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.tasks.BaseKapt
import schwarz.it.lightsaber.gradle.LightsaberExtension
import schwarz.it.lightsaber.gradle.LightsaberTask
import schwarz.it.lightsaber.gradle.registerTask
import schwarz.it.lightsaber.gradle.withDaggerCompiler

fun Project.applyAndroidAnnotationProcessor(extension: LightsaberExtension) {
    extensions.configure<BaseExtension>("android") { androidExtension ->
        val defaultBuildType by lazy(LazyThreadSafetyMode.NONE) { androidExtension.getDefaultBuildType() }
        val defaultFlavour by lazy(LazyThreadSafetyMode.NONE) { androidExtension.getDefaultFlavours() }

        extensions.configure<AndroidComponentsExtension<*, *, *>>("androidComponents") { androidComponents ->
            withDaggerCompiler("annotationProcessor") {
                val lightsaberCheck = tasks.register("lightsaberCheck")
                tasks.named("check").configure { it.dependsOn(lightsaberCheck) }
            }
            withDaggerCompiler("kapt") {
                val lightsaberCheck = tasks.register("lightsaberCheck")
                tasks.named("check").configure { it.dependsOn(lightsaberCheck) }
            }
            withDaggerCompiler("ksp") {
                val lightsaberCheck = tasks.register("lightsaberCheck")
                tasks.named("check").configure { it.dependsOn(lightsaberCheck) }
            }
            androidComponents.onVariants { variant ->
                withDaggerCompiler("annotationProcessor") {
                    val lightsaberVariantCheck = registerAndroidAnnotationProcessorTask(extension, variant)

                    if (variant.buildType == defaultBuildType.name && variant.productFlavors.toSet() == defaultFlavour) {
                        tasks.named("lightsaberCheck").configure { it.dependsOn(lightsaberVariantCheck) }
                    }
                }
                withDaggerCompiler("kapt") {
                    val lightsaberVariantCheck = registerAndroidKaptTask(extension, variant)

                    if (variant.buildType == defaultBuildType.name && variant.productFlavors.toSet() == defaultFlavour) {
                        tasks.named("lightsaberCheck").configure { it.dependsOn(lightsaberVariantCheck) }
                    }
                }
                withDaggerCompiler("ksp") {
                    val lightsaberVariantCheck = registerAndroidKspTask(extension, variant)

                    if (variant.buildType == defaultBuildType.name && variant.productFlavors.toSet() == defaultFlavour) {
                        tasks.named("lightsaberCheck").configure { it.dependsOn(lightsaberVariantCheck) }
                    }
                }
            }
        }
    }
}

private fun Project.registerAndroidAnnotationProcessorTask(
    extension: LightsaberExtension,
    variant: Variant,
): TaskProvider<LightsaberTask> {
    val variantName = variant.name.capitalized()
    val lightsaberVariantCheck = registerTask(extension, variantName)
    lightsaberVariantCheck.configure { task ->
        val taskProvider = provider {
            tasks.withType(JavaCompile::class.java)
                .matching { it.name.startsWith("compile$variantName") }
        }
        task.dependsOn(taskProvider)

        task.source = taskProvider.get()
            .map { fileTree(it.destinationDirectory.dir("schwarz/it/lightsaber")).asFileTree }
            .reduce { acc, fileTree -> acc.plus(fileTree) }
            .matching { it.include("*.lightsaber") }
    }
    return lightsaberVariantCheck
}

private fun Project.registerAndroidKaptTask(
    extension: LightsaberExtension,
    variant: Variant,
): TaskProvider<LightsaberTask> {
    val variantName = variant.name.capitalized()
    val lightsaberVariantCheck = registerTask(extension, variantName)
    lightsaberVariantCheck.configure { task ->
        val taskProvider = provider {
            tasks.withType(BaseKapt::class.java)
                .matching { it.name.startsWith("kapt$variantName") }
        }
        task.dependsOn(taskProvider)

        task.source = taskProvider.get()
            .map { fileTree(it.classesDir.dir("schwarz/it/lightsaber")).asFileTree }
            .reduce { acc, fileTree -> acc.plus(fileTree) }
            .matching { it.include("*.lightsaber") }
    }
    return lightsaberVariantCheck
}

private fun Project.registerAndroidKspTask(
    extension: LightsaberExtension,
    variant: Variant,
): TaskProvider<LightsaberTask> {
    val variantName = variant.name.capitalized()
    val lightsaberVariantCheck = registerTask(extension, variantName)
    lightsaberVariantCheck.configure { task ->
        val taskProvider = provider {
            tasks.withType(KspTaskJvm::class.java)
                .matching { it.name.startsWith("ksp$variantName") }
        }
        task.dependsOn(taskProvider)

        task.source = taskProvider.get()
            .map { fileTree(it.destination.get().resolve("resources/schwarz/it/lightsaber")).asFileTree }
            .reduce { acc, fileTree -> acc.plus(fileTree) }
            .matching { it.include("*.lightsaber") }
    }
    return lightsaberVariantCheck
}

private fun BaseExtension.getDefaultBuildType(): BuildType {
    return buildTypes.firstOrNull { it.isDefault } ?: buildTypes.first()
}

private fun BaseExtension.getDefaultFlavours(): Set<Pair<String?, String?>> {
    return productFlavors
        .groupBy { it.dimension }
        .mapValues { (_, value) ->
            value.firstOrNull { it.isDefault } ?: value.firstOrNull()
        }
        .map { it.key to it.value?.name }
        .toSet()
}
