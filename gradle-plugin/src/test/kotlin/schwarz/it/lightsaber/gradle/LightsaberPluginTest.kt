package schwarz.it.lightsaber.gradle

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import com.google.common.truth.Truth.assertThat
import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedClass
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import schwarz.it.lightsaber.gradle.truth.assertThat

class LightsaberPluginTest {
    @Test
    fun lightsaberWithoutDaggerCompiler() {
        val project = createProject(null)

        assertThat(project).doesntHaveTask("lightsaberCheck")

        assertThat(project).doesntHasDependency(LIGHTSABER)
    }

    @Test
    fun lightsaberWithDaggerCompiler_annotationProcessor() {
        val project = createProject(Processor.AnnotationProcessor)

        assertThat(project).hasTask("check")
            .dependsOn("lightsaberCheck")
            .hasDescription("Check for unused dagger code.")
            .dependsExactlyOn("compileJava", "compileTestJava")

        assertThat(project).hasConfiguration("annotationProcessor")
            .contains(LIGHTSABER)
    }

    @Test
    fun lightsaberWithDaggerCompiler_kapt() {
        val project = createProject(Processor.Kapt)

        assertThat(project).hasTask("check")
            .dependsOn("lightsaberCheck")
            .hasDescription("Check for unused dagger code.")
            .dependsExactlyOn("kaptKotlin", "kaptTestKotlin")

        assertThat(project).hasConfiguration("kapt")
            .contains(LIGHTSABER)
    }

    @ParameterizedTest
    @EnumSource(names = ["Ksp1", "Ksp2"])
    fun lightsaberWithDaggerCompiler_ksp(processor: Processor) {
        val project = createProject(processor)

        assertThat(project).hasTask("check")
            .dependsOn("lightsaberCheck")
            .hasDescription("Check for unused dagger code.")
            .dependsExactlyOn("kspKotlin", "kspTestKotlin")

        assertThat(project).hasConfiguration("ksp")
            .contains(LIGHTSABER)
    }

    @ParameterizedTest
    @EnumSource(AndroidProject::class)
    fun lightsaberWithoutDaggerCompiler_android(type: AndroidProject) {
        val project = createAndroidProject(type, null)

        assertThat(project).doesntHaveTask("lightsaberCheck")
        assertThat(project).doesntHaveTask("lightsaberDebugCheck")
        assertThat(project).doesntHaveTask("lightsaberReleaseCheck")

        assertThat(project).doesntHasDependency(LIGHTSABER)
    }

    @Nested
    inner class AndroidAnnotationProcessor {

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_tasks(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.AnnotationProcessor)

            assertThat(project).hasTask("lightsaberCheck")
                .hasDescription("Check for unused dagger code on the default variant.")

            assertThat(project).hasTask("lightsaberDebugCheck")
                .hasDescription("Check for unused dagger code on the debug variant.")
                .dependsExactlyOn(
                    "compileDebugAndroidTestJavaWithJavac",
                    "compileDebugJavaWithJavac",
                    "compileDebugUnitTestJavaWithJavac",
                )

            assertThat(project).hasTask("lightsaberReleaseCheck")
                .hasDescription("Check for unused dagger code on the release variant.")
                .dependsExactlyOn(
                    "compileReleaseJavaWithJavac",
                    "compileReleaseUnitTestJavaWithJavac",
                )

            assertThat(project).hasConfiguration("annotationProcessor")
                .contains(LIGHTSABER)
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.AnnotationProcessor)

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_defaultDisabled(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.AnnotationProcessor) {
                androidComponents {
                    it.beforeVariants { variantBuilder ->
                        if (variantBuilder.buildType == "debug") {
                            variantBuilder.enable = false
                        }
                    }
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberReleaseCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_default(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.AnnotationProcessor) {
                extensions.configure<BaseExtension>("android") {
                    it.buildTypes.getByName("release") { buildType ->
                        buildType.isDefault = true
                    }
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberReleaseCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_tasks_1flavors(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.AnnotationProcessor) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
            }

            assertThat(project).hasTask("lightsaberCheck")
                .hasDescription("Check for unused dagger code on the default variant.")

            assertThat(project).hasTask("lightsaberStagingDebugCheck")
                .hasDescription("Check for unused dagger code on the stagingDebug variant.")
                .dependsExactlyOn(
                    "compileStagingDebugAndroidTestJavaWithJavac",
                    "compileStagingDebugJavaWithJavac",
                    "compileStagingDebugUnitTestJavaWithJavac",
                )

            assertThat(project).hasTask("lightsaberStagingReleaseCheck")
                .hasDescription("Check for unused dagger code on the stagingRelease variant.")
                .dependsExactlyOn(
                    "compileStagingReleaseJavaWithJavac",
                    "compileStagingReleaseUnitTestJavaWithJavac",
                )

            assertThat(project).hasTask("lightsaberProductionDebugCheck")
                .hasDescription("Check for unused dagger code on the productionDebug variant.")
                .dependsExactlyOn(
                    "compileProductionDebugAndroidTestJavaWithJavac",
                    "compileProductionDebugJavaWithJavac",
                    "compileProductionDebugUnitTestJavaWithJavac",
                )

            assertThat(project).hasTask("lightsaberProductionReleaseCheck")
                .hasDescription("Check for unused dagger code on the productionRelease variant.")
                .dependsExactlyOn(
                    "compileProductionReleaseJavaWithJavac",
                    "compileProductionReleaseUnitTestJavaWithJavac",
                )
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_1flavors(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.AnnotationProcessor) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberProductionDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_1flavors_default(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.AnnotationProcessor) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                        flavor.isDefault = true
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberStagingDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_2flavors(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.AnnotationProcessor) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment", "store")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("google") { flavor ->
                        flavor.dimension = "store"
                    }
                    it.productFlavors.register("huawei") { flavor ->
                        flavor.dimension = "store"
                    }
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberProductionGoogleDebugCheck")
        }
    }

    @Nested
    inner class AndroidKapt {

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_tasks(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.Kapt)

            assertThat(project).hasTask("lightsaberCheck")
                .hasDescription("Check for unused dagger code on the default variant.")

            assertThat(project).hasTask("lightsaberDebugCheck")
                .hasDescription("Check for unused dagger code on the debug variant.")
                .dependsExactlyOn(
                    "kaptDebugAndroidTestKotlin",
                    "kaptDebugKotlin",
                    "kaptDebugUnitTestKotlin",
                )

            assertThat(project).hasTask("lightsaberReleaseCheck")
                .hasDescription("Check for unused dagger code on the release variant.")
                .dependsExactlyOn(
                    "kaptReleaseKotlin",
                    "kaptReleaseUnitTestKotlin",
                )

            assertThat(project).hasConfiguration("kapt")
                .contains(LIGHTSABER)
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.Kapt)

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_default(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.Kapt) {
                extensions.configure<BaseExtension>("android") {
                    it.buildTypes.getByName("release") { buildType ->
                        buildType.isDefault = true
                    }
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberReleaseCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_tasks_1flavors(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.Kapt) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
            }

            assertThat(project).hasTask("lightsaberCheck")
                .hasDescription("Check for unused dagger code on the default variant.")

            assertThat(project).hasTask("lightsaberStagingDebugCheck")
                .hasDescription("Check for unused dagger code on the stagingDebug variant.")
                .dependsExactlyOn(
                    "kaptStagingDebugAndroidTestKotlin",
                    "kaptStagingDebugKotlin",
                    "kaptStagingDebugUnitTestKotlin",
                )

            assertThat(project).hasTask("lightsaberStagingReleaseCheck")
                .hasDescription("Check for unused dagger code on the stagingRelease variant.")
                .dependsExactlyOn(
                    "kaptStagingReleaseKotlin",
                    "kaptStagingReleaseUnitTestKotlin",
                )

            assertThat(project).hasTask("lightsaberProductionDebugCheck")
                .hasDescription("Check for unused dagger code on the productionDebug variant.")
                .dependsExactlyOn(
                    "kaptProductionDebugAndroidTestKotlin",
                    "kaptProductionDebugKotlin",
                    "kaptProductionDebugUnitTestKotlin",
                )

            assertThat(project).hasTask("lightsaberProductionReleaseCheck")
                .hasDescription("Check for unused dagger code on the productionRelease variant.")
                .dependsExactlyOn(
                    "kaptProductionReleaseKotlin",
                    "kaptProductionReleaseUnitTestKotlin",
                )
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_1flavors(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.Kapt) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberProductionDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_1flavors_default(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.Kapt) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                        flavor.isDefault = true
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberStagingDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_2flavors(type: AndroidProject) {
            val project = createAndroidProject(type, Processor.Kapt) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment", "store")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("google") { flavor ->
                        flavor.dimension = "store"
                    }
                    it.productFlavors.register("huawei") { flavor ->
                        flavor.dimension = "store"
                    }
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberProductionGoogleDebugCheck")
        }
    }

    @Nested
    @ParameterizedClass
    @EnumSource(names = ["Ksp1", "Ksp2"])
    inner class AndroidKsp(val processor: Processor) {

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_tasks(type: AndroidProject) {
            val project = createAndroidProject(type, processor)

            assertThat(project).hasTask("lightsaberCheck")
                .hasDescription("Check for unused dagger code on the default variant.")

            assertThat(project).hasTask("lightsaberDebugCheck")
                .hasDescription("Check for unused dagger code on the debug variant.")
                .dependsExactlyOn(
                    "kspDebugAndroidTestKotlin",
                    "kspDebugKotlin",
                    "kspDebugUnitTestKotlin",
                )

            assertThat(project).hasTask("lightsaberReleaseCheck")
                .hasDescription("Check for unused dagger code on the release variant.")
                .dependsExactlyOn(
                    "kspReleaseKotlin",
                    "kspReleaseUnitTestKotlin",
                )

            assertThat(project).hasConfiguration("ksp")
                .contains(LIGHTSABER)
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check(type: AndroidProject) {
            val project = createAndroidProject(type, processor)

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_default(type: AndroidProject) {
            val project = createAndroidProject(type, processor) {
                extensions.configure<BaseExtension>("android") {
                    it.buildTypes.getByName("release") { buildType ->
                        buildType.isDefault = true
                    }
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberReleaseCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_tasks_1flavors(type: AndroidProject) {
            val project = createAndroidProject(type, processor) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
            }

            assertThat(project).hasTask("lightsaberCheck")
                .hasDescription("Check for unused dagger code on the default variant.")

            assertThat(project).hasTask("lightsaberStagingDebugCheck")
                .hasDescription("Check for unused dagger code on the stagingDebug variant.")
                .dependsExactlyOn(
                    "kspStagingDebugAndroidTestKotlin",
                    "kspStagingDebugKotlin",
                    "kspStagingDebugUnitTestKotlin",
                )

            assertThat(project).hasTask("lightsaberStagingReleaseCheck")
                .hasDescription("Check for unused dagger code on the stagingRelease variant.")
                .dependsExactlyOn(
                    "kspStagingReleaseKotlin",
                    "kspStagingReleaseUnitTestKotlin",
                )

            assertThat(project).hasTask("lightsaberProductionDebugCheck")
                .hasDescription("Check for unused dagger code on the productionDebug variant.")
                .dependsExactlyOn(
                    "kspProductionDebugAndroidTestKotlin",
                    "kspProductionDebugKotlin",
                    "kspProductionDebugUnitTestKotlin",
                )

            assertThat(project).hasTask("lightsaberProductionReleaseCheck")
                .hasDescription("Check for unused dagger code on the productionRelease variant.")
                .dependsExactlyOn(
                    "kspProductionReleaseKotlin",
                    "kspProductionReleaseUnitTestKotlin",
                )
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_1flavors(type: AndroidProject) {
            val project = createAndroidProject(type, processor) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberProductionDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_1flavors_default(type: AndroidProject) {
            val project = createAndroidProject(type, processor) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                        flavor.isDefault = true
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberStagingDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_2flavors(type: AndroidProject) {
            val project = createAndroidProject(type, processor) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment", "store")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("google") { flavor ->
                        flavor.dimension = "store"
                    }
                    it.productFlavors.register("huawei") { flavor ->
                        flavor.dimension = "store"
                    }
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberProductionGoogleDebugCheck")
        }
    }

    @Nested
    inner class NoSupportDaggerVersion {

        @ParameterizedTest
        @EnumSource(Processor::class)
        fun noAndroid(processor: Processor) {
            try {
                createProject(processor, version = "2.47")
                fail("wtf?")
            } catch (e: GradleException) {
                assertThat(e)
                    .hasCauseThat()
                    .hasMessageThat()
                    .isEqualTo("This version of lightsaber only supports dagger 2.48 or greater")
            }
        }

        @ParameterizedTest
        @EnumSource(Processor::class)
        fun android(processor: Processor) {
            try {
                createAndroidProject(AndroidProject.Application, processor, version = "2.47")
                fail("wtf?")
            } catch (e: GradleException) {
                assertThat(e)
                    .hasCauseThat()
                    .hasCauseThat()
                    .hasMessageThat()
                    .isEqualTo("This version of lightsaber only supports dagger 2.48 or greater")
            }
        }
    }
}

private fun Project.androidComponents(block: (AndroidComponentsExtension<*, *, *>) -> Unit) {
    project.extensions.configure<AndroidComponentsExtension<*, *, *>>("androidComponents") {
        block(it)
    }
}

private fun createProject(
    processor: Processor?,
    version: String = "2.48",
    block: Project.() -> Unit = { /* no-op */ },
): Project {
    val project = ProjectBuilder.builder()
        .build()

    project.pluginManager.apply(LightsaberPlugin::class.java)
    project.pluginManager.apply(JavaPlugin::class.java)
    when (processor) {
        Processor.AnnotationProcessor -> Unit

        Processor.Kapt -> {
            project.pluginManager.apply(KotlinPluginWrapper::class.java)
            project.pluginManager.apply(Kapt3GradleSubplugin::class.java)
        }

        Processor.Ksp1 -> {
            project.pluginManager.apply(KotlinPluginWrapper::class.java)
            project.pluginManager.apply(KspGradleSubplugin::class.java)
            project.extensions.configure<KspExtension> {
                useKsp2.set(false)
            }
        }

        Processor.Ksp2 -> {
            project.pluginManager.apply(KotlinPluginWrapper::class.java)
            project.pluginManager.apply(KspGradleSubplugin::class.java)
            project.extensions.configure<KspExtension> {
                useKsp2.set(true)
            }
        }

        null -> {
            project.pluginManager.apply(KotlinPluginWrapper::class.java)
        }
    }

    if (processor != null) {
        project.dependencies {
            processor.configuration("com.google.dagger:dagger-compiler:$version")
        }
    }

    project.block()

    project.evaluate()

    return project
}

private fun createAndroidProject(
    type: AndroidProject,
    processor: Processor?,
    version: String = "2.48",
    block: Project.() -> Unit = { /* no-op */ },
): Project {
    val project = ProjectBuilder.builder()
        .withName("test-project")
        .build()

    when (type) {
        AndroidProject.Application -> project.pluginManager.apply("com.android.application")
        AndroidProject.Library -> project.pluginManager.apply("com.android.library")
    }
    project.pluginManager.apply(LightsaberPlugin::class.java)
    when (processor) {
        Processor.AnnotationProcessor -> Unit

        Processor.Kapt -> {
            project.pluginManager.apply(KotlinAndroidPluginWrapper::class.java)
            project.pluginManager.apply(Kapt3GradleSubplugin::class.java)
        }

        Processor.Ksp1 -> {
            project.pluginManager.apply(KotlinAndroidPluginWrapper::class.java)
            project.pluginManager.apply(KspGradleSubplugin::class.java)
            project.extensions.configure<KspExtension> {
                useKsp2.set(false)
            }
        }

        Processor.Ksp2 -> {
            project.pluginManager.apply(KotlinAndroidPluginWrapper::class.java)
            project.pluginManager.apply(KspGradleSubplugin::class.java)
            project.extensions.configure<KspExtension> {
                useKsp2.set(true)
            }
        }

        null -> {
            project.pluginManager.apply(KotlinAndroidPluginWrapper::class.java)
        }
    }

    if (processor != null) {
        project.dependencies {
            processor.configuration("com.google.dagger:dagger-compiler:$version")
        }
    }

    project.extensions.configure<BaseExtension>("android") {
        it.compileSdkVersion(21)
        it.namespace = "com.example"
    }

    project.block()

    project.evaluate()

    return project
}

enum class AndroidProject { Application, Library }

enum class Processor(val configuration: String) {
    AnnotationProcessor("annotationProcessor"),
    Kapt("kapt"),
    Ksp1("ksp"),
    Ksp2("ksp"),
}

private fun Project.evaluate() {
    (this as ProjectInternal).evaluate()
}

private const val LIGHTSABER = "io.github.schwarzit:lightsaber"
