package schwarz.it.lightsaber.gradle

import com.android.build.gradle.BaseExtension
import com.google.common.truth.Truth.assertThat
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.dependencies
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.KotlinAndroidPluginWrapper
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import schwarz.it.lightsaber.gradle.truth.assertThat

class LightsaberPluginTest {
    @Test
    fun lightsaberWithoutDaggerCompiler() {
        val project = createProject {
            // no-op
        }

        assertThat(project).doesntHaveTask("lightsaberCheck")

        assertThat(project).doesntHasDependency(LIGHTSABER)
    }

    @Test
    fun lightsaberWithDaggerCompiler_annotationProcessor() {
        val project = createProject {
            dependencies {
                "annotationProcessor"(daggerCompiler())
            }
        }

        assertThat(project).hasTask("check")
            .dependsOn("lightsaberCheck")
            .hasDescription("Check for unused dagger code.")
            .dependsExactlyOn("compileJava", "compileTestJava")

        assertThat(project).hasConfiguration("annotationProcessor")
            .contains(LIGHTSABER)
    }

    @Test
    fun lightsaberWithDaggerCompiler_kapt() {
        val project = createProject {
            dependencies {
                "kapt"(daggerCompiler())
            }
        }

        assertThat(project).hasTask("check")
            .dependsOn("lightsaberCheck")
            .hasDescription("Check for unused dagger code.")
            .dependsExactlyOn("kaptKotlin", "kaptTestKotlin")

        assertThat(project).hasConfiguration("kapt")
            .contains(LIGHTSABER)
    }

    @Test
    fun lightsaberWithDaggerCompiler_ksp() {
        val project = createProject {
            dependencies {
                "ksp"(daggerCompiler())
            }
        }

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
        val project = createAndroidProject(type) {
            // no-op
        }

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
            val project = createAndroidProject(type) {
                dependencies {
                    "annotationProcessor"(daggerCompiler())
                }
            }

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
            val project = createAndroidProject(type) {
                dependencies {
                    "annotationProcessor"(daggerCompiler())
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_default(type: AndroidProject) {
            val project = createAndroidProject(type) {
                extensions.configure<BaseExtension>("android") {
                    it.buildTypes.getByName("release") { buildType ->
                        buildType.isDefault = true
                    }
                }
                dependencies {
                    "annotationProcessor"(daggerCompiler())
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberReleaseCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_tasks_1flavors(type: AndroidProject) {
            val project = createAndroidProject(type) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
                dependencies {
                    "annotationProcessor"(daggerCompiler())
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
            val project = createAndroidProject(type) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
                dependencies {
                    "annotationProcessor"(daggerCompiler())
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberProductionDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_1flavors_default(type: AndroidProject) {
            val project = createAndroidProject(type) {
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
                dependencies {
                    "annotationProcessor"(daggerCompiler())
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberStagingDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_2flavors(type: AndroidProject) {
            val project = createAndroidProject(type) {
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
                dependencies {
                    "annotationProcessor"(daggerCompiler())
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
            val project = createAndroidProject(type) {
                dependencies {
                    "kapt"(daggerCompiler())
                }
            }

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
            val project = createAndroidProject(type) {
                dependencies {
                    "kapt"(daggerCompiler())
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_default(type: AndroidProject) {
            val project = createAndroidProject(type) {
                extensions.configure<BaseExtension>("android") {
                    it.buildTypes.getByName("release") { buildType ->
                        buildType.isDefault = true
                    }
                }
                dependencies {
                    "kapt"(daggerCompiler())
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberReleaseCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_tasks_1flavors(type: AndroidProject) {
            val project = createAndroidProject(type) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
                dependencies {
                    "kapt"(daggerCompiler())
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
            val project = createAndroidProject(type) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
                dependencies {
                    "kapt"(daggerCompiler())
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberProductionDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_1flavors_default(type: AndroidProject) {
            val project = createAndroidProject(type) {
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
                dependencies {
                    "kapt"(daggerCompiler())
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberStagingDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_2flavors(type: AndroidProject) {
            val project = createAndroidProject(type) {
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
                dependencies {
                    "kapt"(daggerCompiler())
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberProductionGoogleDebugCheck")
        }
    }

    @Nested
    inner class AndroidKsp {

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_tasks(type: AndroidProject) {
            val project = createAndroidProject(type) {
                dependencies {
                    "ksp"(daggerCompiler())
                }
            }

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
            val project = createAndroidProject(type) {
                dependencies {
                    "ksp"(daggerCompiler())
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_default(type: AndroidProject) {
            val project = createAndroidProject(type) {
                extensions.configure<BaseExtension>("android") {
                    it.buildTypes.getByName("release") { buildType ->
                        buildType.isDefault = true
                    }
                }
                dependencies {
                    "ksp"(daggerCompiler())
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberReleaseCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_tasks_1flavors(type: AndroidProject) {
            val project = createAndroidProject(type) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
                dependencies {
                    "ksp"(daggerCompiler())
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
            val project = createAndroidProject(type) {
                extensions.configure<BaseExtension>("android") {
                    it.flavorDimensions("environment")
                    it.productFlavors.register("staging") { flavor ->
                        flavor.dimension = "environment"
                    }
                    it.productFlavors.register("production") { flavor ->
                        flavor.dimension = "environment"
                    }
                }
                dependencies {
                    "ksp"(daggerCompiler())
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberProductionDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_1flavors_default(type: AndroidProject) {
            val project = createAndroidProject(type) {
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
                dependencies {
                    "ksp"(daggerCompiler())
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberStagingDebugCheck")
        }

        @ParameterizedTest
        @EnumSource(AndroidProject::class)
        fun lightsaberWithDaggerCompiler_check_2flavors(type: AndroidProject) {
            val project = createAndroidProject(type) {
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
                dependencies {
                    "ksp"(daggerCompiler())
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
                createProject {
                    dependencies {
                        processor.configuration(daggerCompiler("2.47"))
                    }
                }
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
                createAndroidProject {
                    dependencies {
                        processor.configuration(daggerCompiler("2.47"))
                    }
                }
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

private fun createProject(block: Project.() -> Unit): Project {
    val project = ProjectBuilder.builder()
        .build()

    project.pluginManager.apply(LightsaberPlugin::class.java)
    project.pluginManager.apply(JavaPlugin::class.java)
    project.pluginManager.apply(KotlinPluginWrapper::class.java)
    project.pluginManager.apply(Kapt3GradleSubplugin::class.java)
    project.pluginManager.apply(KspGradleSubplugin::class.java)

    project.block()

    project.evaluate()

    return project
}

private fun createAndroidProject(
    type: AndroidProject = AndroidProject.Application,
    block: Project.() -> Unit,
): Project {
    val project = ProjectBuilder.builder()
        .withName("test-project")
        .build()

    when (type) {
        AndroidProject.Application -> project.pluginManager.apply("com.android.application")
        AndroidProject.Library -> project.pluginManager.apply("com.android.library")
    }
    project.pluginManager.apply(LightsaberPlugin::class.java)
    project.pluginManager.apply(KotlinAndroidPluginWrapper::class.java)
    project.pluginManager.apply(Kapt3GradleSubplugin::class.java)
    project.pluginManager.apply(KspGradleSubplugin::class.java)

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
    Ksp("ksp"),
}

private fun Project.evaluate() {
    (this as ProjectInternal).evaluate()
}

private fun daggerCompiler(version: String = "2.48"): String {
    return "com.google.dagger:dagger-compiler:$version"
}

private const val LIGHTSABER = "io.github.schwarzit:lightsaber"
