package schwarz.it.lightsaber.gradle

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import schwarz.it.lightsaber.gradle.truth.assertThat
import java.io.File
import java.nio.file.Files

class LightsaberPluginIntegrationTest {

    @Test
    fun annotationProcessor() {
        val buildResult = GradleRunner.create()
            .withProjectDirFromResources("annotationProcessor")
            .withPluginClasspath()
            .withArguments("lightsaberCheck")
            .buildAndFail()

        assertThat(buildResult).hasTask(":compileJava")
        assertThat(buildResult).hasTask(":compileTestJava")
        assertThat(buildResult).hasTask(":lightsaberCheck").hasOutcome(TaskOutcome.FAILED)

        assertThat(buildResult).contains("MyModule.java:15:17: The @Provides `myLong` declared in `com.example.MyModule` is not used. [UnusedBindsAndProvides]")
        assertThat(buildResult).contains("> Analysis failed with 1 error")
    }

    @Test
    fun kapt() {
        val buildResult = GradleRunner.create()
            .withProjectDirFromResources("kapt")
            .withPluginClasspath()
            .withArguments("lightsaberCheck")
            .buildAndFail()

        assertThat(buildResult).hasTask(":kaptKotlin")
        assertThat(buildResult).hasTask(":kaptTestKotlin")
        assertThat(buildResult).hasTask(":lightsaberCheck").hasOutcome(TaskOutcome.FAILED)

        assertThat(buildResult).contains("MyModule.java:26:27: The @Provides `myLong` declared in `com.example.MyModule` is not used. [UnusedBindsAndProvides]")
        assertThat(buildResult).contains("> Analysis failed with 1 error")
    }

    @Test
    fun ksp() {
        val buildResult = GradleRunner.create()
            .withProjectDirFromResources("ksp")
            .withPluginClasspath()
            .withArguments("lightsaberCheck")
            .buildAndFail()

        assertThat(buildResult).hasTask(":kspKotlin")
        assertThat(buildResult).hasTask(":kspTestKotlin")
        assertThat(buildResult).hasTask(":lightsaberCheck").hasOutcome(TaskOutcome.FAILED)

        assertThat(buildResult).contains("MyComponent.kt:22: The @Provides `myLong` declared in `com.example.MyModule` is not used. [UnusedBindsAndProvides]")
        assertThat(buildResult).contains("> Analysis failed with 1 error")
    }
}

// From https://github.com/detekt/detekt/blob/92a5aa5624d8d7bd20ee70ed24cfccc208b25fdb/detekt-gradle-plugin/src/testFixtures/kotlin/io/gitlab/arturbosch/detekt/testkit/GradleRunnerExtensions.kt#L7-L16
/**
 * Copy project files from `resources` to temporary directories for isolation.
 * This helps with the incremental build (up-to-date checks).
 */
private fun GradleRunner.withProjectDirFromResources(resourcePath: String) = apply {
    val resourceDir = File(javaClass.classLoader.getResource("testProjects/$resourcePath")!!.file)
    val projectDir = Files.createTempDirectory(resourcePath).toFile()
    resourceDir.copyRecursively(projectDir)
    withProjectDir(projectDir)
}
