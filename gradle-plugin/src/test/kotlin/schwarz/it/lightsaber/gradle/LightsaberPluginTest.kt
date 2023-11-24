package schwarz.it.lightsaber.gradle

import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.dependencies
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
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

private fun Project.evaluate() {
    (this as ProjectInternal).evaluate()
}

private const val DAGGER_COMPILER = "com.google.dagger:dagger-compiler:2.48.1"
private const val LIGHTSABER = "io.github.schwarzit:lightsaber"
