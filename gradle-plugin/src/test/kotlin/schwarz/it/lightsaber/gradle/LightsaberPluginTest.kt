package schwarz.it.lightsaber.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class LightsaberPluginTest {

    @Test
    fun annotationProcessor() {
        val buildResult = GradleRunner.create()
            .withProjectDirFromResources("annotationProcessor")
            .withPluginClasspath()
            .withArguments("lightsaberCheck")
            .buildAndFail()

        println(buildResult.output)

        assertThat(buildResult.task(":kaptKotlin")).isNull()
        assertThat(buildResult.task(":kaptTestKotlin")).isNull()
        assertThat(buildResult.task(":compileTestJava")).isNotNull()
        assertThat(buildResult.task(":lightsaberCheck")!!.outcome).isEqualTo(TaskOutcome.FAILED)

        assertThat(buildResult.output).contains("MyComponent.java:8:8: The @Provides `myLong` declared on `schwarz.it.lightsaber.sample.MyModule` is not used. [UnusedBindsAndProvides]")
        assertThat(buildResult.output).contains("> Analysis failed with 1 error")
    }

    @Test
    fun kapt() {
        val buildResult = GradleRunner.create()
            .withProjectDirFromResources("kapt")
            .withPluginClasspath()
            .withArguments("lightsaberCheck")
            .buildAndFail()

        assertThat(buildResult.task(":kaptKotlin")).isNotNull()
        assertThat(buildResult.task(":kaptTestKotlin")).isNotNull()
        // TODO this shouldn't run assertThat(buildResult.task(":compileTestJava")).isNull()
        assertThat(buildResult.task(":lightsaberCheck")!!.outcome).isEqualTo(TaskOutcome.FAILED)

        assertThat(buildResult.output).contains("MyComponent.java:5:17: The @Provides `myLong` declared on `com.example.MyModule` is not used. [UnusedBindsAndProvides]")
        assertThat(buildResult.output).contains("> Analysis failed with 1 error")
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
