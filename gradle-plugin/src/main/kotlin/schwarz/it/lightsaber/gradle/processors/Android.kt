package schwarz.it.lightsaber.gradle.processors

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.BuildType
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin
import schwarz.it.lightsaber.gradle.LightsaberExtension

fun Project.applyAndroidAnnotationProcessor(extension: LightsaberExtension) {
    extensions.configure<BaseExtension>("android") { androidExtension ->
        val defaultBuildType by lazy(LazyThreadSafetyMode.NONE) { androidExtension.getDefaultBuildType() }
        val defaultFlavour by lazy(LazyThreadSafetyMode.NONE) { androidExtension.getDefaultFlavours() }

        extensions.configure<AndroidComponentsExtension<*, *, *>>("androidComponents") { androidComponents ->
            withDaggerCompiler {
                val lightsaberCheck = tasks.register("lightsaberCheck") {
                    it.group = LifecycleBasePlugin.VERIFICATION_GROUP
                    it.description = "Check for unused dagger code on the default variant."
                }
                tasks.named("check").configure { it.dependsOn(lightsaberCheck) }
            }

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

                    if (variant.buildType == defaultBuildType.name && variant.productFlavors.toSet() == defaultFlavour) {
                        tasks.named("lightsaberCheck").configure { it.dependsOn(lightsaberVariantCheck) }
                    }
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
