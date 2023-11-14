package schwarz.it.lightsaber.gradle

import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import schwarz.it.lightsaber.gradle.truth.assertThat

class LightsaberPluginTest {
    @Test
    fun lightsaberOnly() {
        val project = createProject {
            // no-op
        }

        assertThat(project).doesntHasTask("lightsaberCheck")
    }

    @Test
    fun lightsaberKotlin() {
        val project = createProject {
            pluginManager.apply(KotlinPluginWrapper::class.java)
        }

        assertThat(project).doesntHasTask("lightsaberCheck")
    }

    @Test
    fun lightsaberJava() {
        val project = createProject {
            pluginManager.apply(JavaPlugin::class.java)
        }

        assertThat(project).doesntHasTask("lightsaberCheck")
    }

    @Test
    fun lightsaberKotlinKapt() {
        val project = createProject {
            pluginManager.apply(KotlinPluginWrapper::class.java)
            pluginManager.apply(Kapt3GradleSubplugin::class.java)
        }

        assertThat(project).hasTask("check")
            .dependsOn("lightsaberCheck")
            .dependsExactlyOn("kaptKotlin", "kaptTestKotlin")
    }

    @Test
    fun lightsaberKotlinKsp() {
        val project = createProject {
            pluginManager.apply(KotlinPluginWrapper::class.java)
            pluginManager.apply(KspGradleSubplugin::class.java)
        }

        assertThat(project).hasTask("check")
            .dependsOn("lightsaberCheck")
            .dependsExactlyOn("kspKotlin", "kspTestKotlin")
    }

    @Disabled("We don't support this yet")
    @Test
    fun lightsaberKotlinKaptAndKsp() {
        val project = createProject {
            pluginManager.apply(KotlinPluginWrapper::class.java)
            pluginManager.apply(Kapt3GradleSubplugin::class.java)
            pluginManager.apply(KspGradleSubplugin::class.java)
        }

        assertThat(project).hasTask("check")
            .dependsOn("lightsaberCheck")
            .dependsExactlyOn("", "a")
    }
}

private fun createProject(block: Project.() -> Unit): Project {
    val project = ProjectBuilder.builder()
        .build()

    project.pluginManager.apply(LightsaberPlugin::class.java)
    project.block()

    project.evaluate()

    return project
}

private fun Project.evaluate() {
    (this as ProjectInternal).evaluate()
}
