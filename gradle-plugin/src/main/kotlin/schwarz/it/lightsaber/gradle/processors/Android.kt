package schwarz.it.lightsaber.gradle.processors

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.Variant
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.BuildType
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin
import schwarz.it.lightsaber.gradle.LightsaberExtension
import schwarz.it.lightsaber.gradle.LightsaberTask

fun Project.applyAndroidAnnotationProcessor(extension: LightsaberExtension) {
    extensions.configure<BaseExtension>("android") { androidExtension ->
        val defaultBuildType by lazy(LazyThreadSafetyMode.NONE) { androidExtension.getDefaultBuildType() }
        val defaultFlavour by lazy(LazyThreadSafetyMode.NONE) { androidExtension.getDefaultFlavours() }

        extensions.configure<AndroidComponentsExtension<*, *, *>>("androidComponents") { androidComponents ->
            val variants: MutableList<Pair<Variant, TaskProvider<LightsaberTask>>> = mutableListOf()

            androidComponents.onVariants { variant ->
                withDaggerCompiler { processor ->
                    val lightsaberVariantCheck = when (processor) {
                        Processor.AnnotationProcessor -> registerAnnotationProcessorTask(extension, variant)
                        Processor.Kapt -> registerKaptTask(extension, variant)
                        Processor.Ksp -> registerKspTask(extension, variant)
                    }

                    lightsaberVariantCheck.configure {
                        it.group = LifecycleBasePlugin.VERIFICATION_GROUP
                        it.description = "Check for unused dagger code on the ${variant.name} variant."
                    }

                    variants.add(variant to lightsaberVariantCheck)
                }
            }

            withDaggerCompiler {
                val lightsaberCheck = tasks.register("lightsaberCheck") {
                    it.group = LifecycleBasePlugin.VERIFICATION_GROUP
                    it.description = "Check for unused dagger code on the default variant."
                }
                tasks.named("check").configure { it.dependsOn(lightsaberCheck) }

                afterEvaluate { project ->
                    val (_, defaultTask) = variants.find { (variant, _) ->
                        variant.buildType == defaultBuildType.name && variant.productFlavors.toSet() == defaultFlavour
                    } ?: variants.firstOrNull() ?: error("The project ${project.name} must have at least one variant")

                    lightsaberCheck.configure { it.dependsOn(defaultTask) }
                }
            }
        }
    }
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
