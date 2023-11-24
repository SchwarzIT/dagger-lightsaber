package schwarz.it.lightsaber.gradle

import com.android.build.gradle.BaseExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
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
                "annotationProcessor"(DAGGER_COMPILER)
            }
        }

        assertThat(project).hasTask("check")
            .dependsOn("lightsaberCheck")
            .dependsExactlyOn("compileJava", "compileTestJava")

        assertThat(project).hasConfiguration("annotationProcessor")
            .contains(LIGHTSABER)
    }

    @Test
    fun lightsaberWithDaggerCompiler_kapt() {
        val project = createProject {
            dependencies {
                "kapt"(DAGGER_COMPILER)
            }
        }

        assertThat(project).hasTask("check")
            .dependsOn("lightsaberCheck")
            .dependsExactlyOn("kaptKotlin", "kaptTestKotlin")

        assertThat(project).hasConfiguration("kapt")
            .contains(LIGHTSABER)
    }

    @Test
    fun lightsaberWithDaggerCompiler_ksp() {
        val project = createProject {
            dependencies {
                "ksp"(DAGGER_COMPILER)
            }
        }

        assertThat(project).hasTask("check")
            .dependsOn("lightsaberCheck")
            .dependsExactlyOn("kspKotlin", "kspTestKotlin")

        assertThat(project).hasConfiguration("ksp")
            .contains(LIGHTSABER)
    }

    @Test
    fun lightsaberWithoutDaggerCompiler_android() {
        val project = createAndroidProject {
            // no-op
        }

        assertThat(project).doesntHaveTask("lightsaberCheck")
        assertThat(project).doesntHaveTask("lightsaberDebugCheck")
        assertThat(project).doesntHaveTask("lightsaberReleaseCheck")

        assertThat(project).doesntHasDependency(LIGHTSABER)
    }

    @Nested
    inner class AndroidAnnotationProcessor {

        @Test
        fun lightsaberWithDaggerCompiler_application_tasks() {
            val project = createAndroidProject {
                dependencies {
                    "annotationProcessor"(DAGGER_COMPILER)
                }
            }

            assertThat(project).hasTask("lightsaberDebugCheck")
                .dependsExactlyOn(
                    "compileDebugAndroidTestJavaWithJavac",
                    "compileDebugJavaWithJavac",
                    "compileDebugUnitTestJavaWithJavac",
                )

            assertThat(project).hasTask("lightsaberReleaseCheck")
                .dependsExactlyOn(
                    "compileReleaseJavaWithJavac",
                    "compileReleaseUnitTestJavaWithJavac",
                )

            assertThat(project).hasConfiguration("annotationProcessor")
                .contains(LIGHTSABER)
        }

        @Test
        fun lightsaberWithDaggerCompiler_application_check() {
            val project = createAndroidProject {
                dependencies {
                    "annotationProcessor"(DAGGER_COMPILER)
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberDebugCheck")
        }

        @Test
        fun lightsaberWithDaggerCompiler_application_check_default() {
            val project = createAndroidProject {
                extensions.configure<BaseExtension>("android") {
                    it.buildTypes.getByName("release") { buildType ->
                        buildType.isDefault = true
                    }
                }
                dependencies {
                    "annotationProcessor"(DAGGER_COMPILER)
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberReleaseCheck")
        }

        @Test
        fun lightsaberWithDaggerCompiler_application_tasks_1flavors() {
            val project = createAndroidProject {
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
                    "annotationProcessor"(DAGGER_COMPILER)
                }
            }

            assertThat(project).hasTask("lightsaberStagingDebugCheck")
                .dependsExactlyOn(
                    "compileStagingDebugAndroidTestJavaWithJavac",
                    "compileStagingDebugJavaWithJavac",
                    "compileStagingDebugUnitTestJavaWithJavac",
                )

            assertThat(project).hasTask("lightsaberStagingReleaseCheck")
                .dependsExactlyOn(
                    "compileStagingReleaseJavaWithJavac",
                    "compileStagingReleaseUnitTestJavaWithJavac",
                )

            assertThat(project).hasTask("lightsaberProductionDebugCheck")
                .dependsExactlyOn(
                    "compileProductionDebugAndroidTestJavaWithJavac",
                    "compileProductionDebugJavaWithJavac",
                    "compileProductionDebugUnitTestJavaWithJavac",
                )

            assertThat(project).hasTask("lightsaberProductionReleaseCheck")
                .dependsExactlyOn(
                    "compileProductionReleaseJavaWithJavac",
                    "compileProductionReleaseUnitTestJavaWithJavac",
                )
        }

        @Test
        fun lightsaberWithDaggerCompiler_application_check_1flavors() {
            val project = createAndroidProject {
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
                    "annotationProcessor"(DAGGER_COMPILER)
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberProductionDebugCheck")
        }

        @Test
        fun lightsaberWithDaggerCompiler_application_check_1flavors_default() {
            val project = createAndroidProject {
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
                    "annotationProcessor"(DAGGER_COMPILER)
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberStagingDebugCheck")
        }

        @Test
        fun lightsaberWithDaggerCompiler_application_check_2flavors() {
            val project = createAndroidProject {
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
                    "annotationProcessor"(DAGGER_COMPILER)
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberProductionGoogleDebugCheck")
        }
    }

    @Nested
    inner class AndroidKapt {

        @Test
        fun lightsaberWithDaggerCompiler_application_tasks() {
            val project = createAndroidProject {
                dependencies {
                    "kapt"(DAGGER_COMPILER)
                }
            }

            assertThat(project).hasTask("lightsaberDebugCheck")
                .dependsExactlyOn(
                    "kaptDebugAndroidTestKotlin",
                    "kaptDebugKotlin",
                    "kaptDebugUnitTestKotlin",
                )

            assertThat(project).hasTask("lightsaberReleaseCheck")
                .dependsExactlyOn(
                    "kaptReleaseKotlin",
                    "kaptReleaseUnitTestKotlin",
                )

            assertThat(project).hasConfiguration("kapt")
                .contains(LIGHTSABER)
        }

        @Test
        fun lightsaberWithDaggerCompiler_application_check() {
            val project = createAndroidProject {
                dependencies {
                    "kapt"(DAGGER_COMPILER)
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberDebugCheck")
        }

        @Test
        fun lightsaberWithDaggerCompiler_application_check_default() {
            val project = createAndroidProject {
                extensions.configure<BaseExtension>("android") {
                    it.buildTypes.getByName("release") { buildType ->
                        buildType.isDefault = true
                    }
                }
                dependencies {
                    "kapt"(DAGGER_COMPILER)
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberReleaseCheck")
        }

        @Test
        fun lightsaberWithDaggerCompiler_application_tasks_1flavors() {
            val project = createAndroidProject {
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
                    "kapt"(DAGGER_COMPILER)
                }
            }

            assertThat(project).hasTask("lightsaberStagingDebugCheck")
                .dependsExactlyOn(
                    "kaptStagingDebugAndroidTestKotlin",
                    "kaptStagingDebugKotlin",
                    "kaptStagingDebugUnitTestKotlin",
                )

            assertThat(project).hasTask("lightsaberStagingReleaseCheck")
                .dependsExactlyOn(
                    "kaptStagingReleaseKotlin",
                    "kaptStagingReleaseUnitTestKotlin",
                )

            assertThat(project).hasTask("lightsaberProductionDebugCheck")
                .dependsExactlyOn(
                    "kaptProductionDebugAndroidTestKotlin",
                    "kaptProductionDebugKotlin",
                    "kaptProductionDebugUnitTestKotlin",
                )

            assertThat(project).hasTask("lightsaberProductionReleaseCheck")
                .dependsExactlyOn(
                    "kaptProductionReleaseKotlin",
                    "kaptProductionReleaseUnitTestKotlin",
                )
        }

        @Test
        fun lightsaberWithDaggerCompiler_application_check_1flavors() {
            val project = createAndroidProject {
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
                    "kapt"(DAGGER_COMPILER)
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberProductionDebugCheck")
        }

        @Test
        fun lightsaberWithDaggerCompiler_application_check_1flavors_default() {
            val project = createAndroidProject {
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
                    "kapt"(DAGGER_COMPILER)
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberStagingDebugCheck")
        }

        @Test
        fun lightsaberWithDaggerCompiler_application_check_2flavors() {
            val project = createAndroidProject {
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
                    "kapt"(DAGGER_COMPILER)
                }
            }

            assertThat(project).hasTask("check")
                .dependsOn("lightsaberCheck")
                .dependsExactlyOn("lightsaberProductionGoogleDebugCheck")
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

private fun createAndroidProject(block: Project.() -> Unit): Project {
    val project = ProjectBuilder.builder()
        .withName("test-project")
        .build()

    project.pluginManager.apply("com.android.application")
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

private fun Project.evaluate() {
    (this as ProjectInternal).evaluate()
}

private const val DAGGER_COMPILER = "com.google.dagger:dagger-compiler:2.48.1"
private const val LIGHTSABER = "io.github.schwarzit:lightsaber"
