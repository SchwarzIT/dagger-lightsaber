package schwarz.it.lightsaber.gradle

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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
        assertThat(buildResult).contains("Foo.java:5:8: The @Inject in `com.example.Foo` constructor is unused because there is a @Provides defined in `com.example.MyModule.foo`. [UnusedInject]")
        assertThat(buildResult).contains("> Analysis failed with 2 errors")
        assertThat(buildResult).doesNotContain("warning:")
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
        assertThat(buildResult).contains("Foo.java:4:14: The @Inject in `com.example.Foo` constructor is unused because there is a @Provides defined in `com.example.MyModule.Companion.provideFoo`. [UnusedInject]")
        assertThat(buildResult).contains("> Analysis failed with 2 errors")
        assertThat(buildResult).doesNotContain("warning:")
    }

    @ParameterizedTest
    @ValueSource(strings = ["ksp1", "ksp2"])
    fun ksp(resourcePath: String) {
        val buildResult = GradleRunner.create()
            .withProjectDirFromResources(resourcePath)
            .withPluginClasspath()
            .withArguments("lightsaberCheck")
            .buildAndFail()

        assertThat(buildResult).hasTask(":kspKotlin")
        assertThat(buildResult).hasTask(":kspTestKotlin")
        assertThat(buildResult).hasTask(":lightsaberCheck").hasOutcome(TaskOutcome.FAILED)

        assertThat(buildResult).contains("MyComponent.kt:24: The @Provides `myLong` declared in `com.example.MyModule` is not used. [UnusedBindsAndProvides]")
        assertThat(buildResult).contains("MyComponent.kt:33: The @Inject in `com.example.Foo` constructor is unused because there is a @Provides defined in `com.example.MyModule.Companion.provideFoo`. [UnusedInject]")
        assertThat(buildResult).contains("> Analysis failed with 2 errors")
    }

    @Test
    fun annotationProcessor_cache() {
        val runner = GradleRunner.create()
            .withProjectDirFromResources("annotationProcessor")
            .withPluginClasspath()
            .withArguments("lightsaberCheck", "--build-cache")

        runner.buildAndFail()

        runner.projectDir.resolve("build/generated/lightsaber").deleteRecursively()

        val buildResult = runner.buildAndFail()

        assertThat(buildResult).hasTask(":compileJava").hasOutcome(TaskOutcome.FROM_CACHE)
        assertThat(buildResult).hasTask(":compileTestJava")
        assertThat(buildResult).hasTask(":lightsaberCheck").hasOutcome(TaskOutcome.FAILED)

        assertThat(buildResult).contains("MyModule.java:15:17: The @Provides `myLong` declared in `com.example.MyModule` is not used. [UnusedBindsAndProvides]")
        assertThat(buildResult).contains("Foo.java:5:8: The @Inject in `com.example.Foo` constructor is unused because there is a @Provides defined in `com.example.MyModule.foo`. [UnusedInject]")
        assertThat(buildResult).contains("> Analysis failed with 2 errors")
        assertThat(buildResult).doesNotContain("warning:")
    }

    @Test
    fun kapt_cache() {
        val runner = GradleRunner.create()
            .withProjectDirFromResources("kapt")
            .withPluginClasspath()
            .withArguments("lightsaberCheck", "--build-cache")

        runner.buildAndFail()

        runner.projectDir.resolve("build/generated/lightsaber").deleteRecursively()

        val buildResult = runner.buildAndFail()

        assertThat(buildResult).hasTask(":kaptKotlin").hasOutcome(TaskOutcome.FROM_CACHE)
        assertThat(buildResult).hasTask(":kaptTestKotlin")
        assertThat(buildResult).hasTask(":lightsaberCheck").hasOutcome(TaskOutcome.FAILED)

        assertThat(buildResult).contains("MyModule.java:26:27: The @Provides `myLong` declared in `com.example.MyModule` is not used. [UnusedBindsAndProvides]")
        assertThat(buildResult).contains("Foo.java:4:14: The @Inject in `com.example.Foo` constructor is unused because there is a @Provides defined in `com.example.MyModule.Companion.provideFoo`. [UnusedInject]")
        assertThat(buildResult).contains("> Analysis failed with 2 errors")
        assertThat(buildResult).doesNotContain("warning:")
    }

    @ParameterizedTest
    @ValueSource(strings = ["ksp1", "ksp2"])
    fun ksp_cache(resourcePath: String) {
        val runner = GradleRunner.create()
            .withProjectDirFromResources(resourcePath)
            .withPluginClasspath()
            .withArguments("lightsaberCheck", "--build-cache")

        runner.buildAndFail()

        runner.projectDir.resolve("build/generated/lightsaber").deleteRecursively()

        val buildResult = runner.buildAndFail()

        assertThat(buildResult).hasTask(":kspKotlin").hasOutcome(TaskOutcome.FROM_CACHE)
        assertThat(buildResult).hasTask(":kspTestKotlin")
        assertThat(buildResult).hasTask(":lightsaberCheck").hasOutcome(TaskOutcome.FAILED)

        assertThat(buildResult).contains("MyComponent.kt:24: The @Provides `myLong` declared in `com.example.MyModule` is not used. [UnusedBindsAndProvides]")
        assertThat(buildResult).contains("MyComponent.kt:33: The @Inject in `com.example.Foo` constructor is unused because there is a @Provides defined in `com.example.MyModule.Companion.provideFoo`. [UnusedInject]")
        assertThat(buildResult).contains("> Analysis failed with 2 errors")
    }

    @Test
    fun androidAnnotationProcessor() {
        val buildResult = GradleRunner.create()
            .withProjectDirFromResources("androidAnnotationProcessor")
            .withPluginClasspath()
            .withArguments("lightsaberCheck")
            .buildAndFail()

        assertThat(buildResult).hasTask(":compileDebugJavaWithJavac")
        assertThat(buildResult).hasTask(":compileDebugAndroidTestJavaWithJavac")
        assertThat(buildResult).hasTask(":compileDebugUnitTestJavaWithJavac")
        assertThat(buildResult).hasTask(":lightsaberDebugCheck").hasOutcome(TaskOutcome.FAILED)

        assertThat(buildResult).contains("MyModule.java:15:17: The @Provides `myLong` declared in `com.example.MyModule` is not used. [UnusedBindsAndProvides]")
        assertThat(buildResult).contains("> Analysis failed with 1 error")
        assertThat(buildResult).doesNotContain("warning:")
    }

    @Test
    fun androidKapt() {
        val buildResult = GradleRunner.create()
            .withProjectDirFromResources("androidKapt")
            .withPluginClasspath()
            .withArguments("lightsaberCheck")
            .buildAndFail()

        assertThat(buildResult).hasTask(":kaptDebugKotlin")
        assertThat(buildResult).hasTask(":kaptDebugUnitTestKotlin")
        assertThat(buildResult).hasTask(":kaptDebugAndroidTestKotlin")
        assertThat(buildResult).hasTask(":lightsaberDebugCheck").hasOutcome(TaskOutcome.FAILED)

        assertThat(buildResult).contains("MyModule.java:26:27: The @Provides `myLong` declared in `com.example.MyModule` is not used. [UnusedBindsAndProvides]")
        assertThat(buildResult).contains("> Analysis failed with 1 error")
        assertThat(buildResult).doesNotContain("warning:")
    }

    @ParameterizedTest
    @ValueSource(strings = ["androidKsp1", "androidKsp2"])
    fun androidKsp(resourcePath: String) {
        val buildResult = GradleRunner.create()
            .withProjectDirFromResources(resourcePath)
            .withPluginClasspath()
            .withArguments("lightsaberCheck")
            .buildAndFail()

        assertThat(buildResult).hasTask(":kspDebugKotlin")
        assertThat(buildResult).hasTask(":kspDebugUnitTestKotlin")
        assertThat(buildResult).hasTask(":kspDebugAndroidTestKotlin")
        assertThat(buildResult).hasTask(":lightsaberDebugCheck").hasOutcome(TaskOutcome.FAILED)

        assertThat(buildResult).contains("MyComponent.kt:22: The @Provides `myLong` declared in `com.example.MyModule` is not used. [UnusedBindsAndProvides]")
        assertThat(buildResult).contains("> Analysis failed with 1 error")
    }
}

/**
 * Copy project files from `resources` to temporary directories for isolation.
 * This helps with the incremental build (up-to-date checks).
 */
// From https://github.com/detekt/detekt/blob/92a5aa5624d8d7bd20ee70ed24cfccc208b25fdb/detekt-gradle-plugin/src/testFixtures/kotlin/io/gitlab/arturbosch/detekt/testkit/GradleRunnerExtensions.kt#L7-L16
internal fun GradleRunner.withProjectDirFromResources(resourcePath: String) = apply {
    val resourceDir = File(javaClass.classLoader.getResource("testProjects/$resourcePath")!!.file)
    val projectDir = Files.createTempDirectory(resourcePath).toFile()
    resourceDir.copyRecursively(projectDir)
    withProjectDir(projectDir)
}
