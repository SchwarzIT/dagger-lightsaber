package schwarz.it.lightsaber.gradle.processors

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.BuildType
import org.gradle.api.Project
import schwarz.it.lightsaber.gradle.LightsaberExtension
import schwarz.it.lightsaber.gradle.withDaggerCompiler

fun Project.applyAndroidAnnotationProcessor(extension: LightsaberExtension) {
    extensions.configure<BaseExtension>("android") { androidExtension ->
        val defaultBuildType by lazy(LazyThreadSafetyMode.NONE) { androidExtension.getDefaultBuildType() }
        val defaultFlavour by lazy(LazyThreadSafetyMode.NONE) { androidExtension.getDefaultFlavours() }

        extensions.configure<AndroidComponentsExtension<*, *, *>>("androidComponents") { androidComponents ->
            withDaggerCompiler {
                val lightsaberCheck = tasks.register("lightsaberCheck")
                tasks.named("check").configure { it.dependsOn(lightsaberCheck) }
            }
            androidComponents.onVariants { variant ->
                withDaggerCompiler("annotationProcessor") {
                    val lightsaberVariantCheck = registerAnnotationProcessorTask(extension, variant)

                    if (variant.buildType == defaultBuildType.name && variant.productFlavors.toSet() == defaultFlavour) {
                        tasks.named("lightsaberCheck").configure { it.dependsOn(lightsaberVariantCheck) }
                    }
                }
                withDaggerCompiler("kapt") {
                    val lightsaberVariantCheck = registerKaptTask(extension, variant)

                    if (variant.buildType == defaultBuildType.name && variant.productFlavors.toSet() == defaultFlavour) {
                        tasks.named("lightsaberCheck").configure { it.dependsOn(lightsaberVariantCheck) }
                    }
                }
                withDaggerCompiler("ksp") {
                    val lightsaberVariantCheck = registerKspTask(extension, variant)

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
