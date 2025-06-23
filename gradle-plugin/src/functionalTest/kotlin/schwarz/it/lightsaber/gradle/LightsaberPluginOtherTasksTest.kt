package schwarz.it.lightsaber.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

class LightsaberPluginOtherTasksTest {

    @ParameterizedTest
    @ValueSource(strings = ["annotationProcessor", "kapt", "ksp1", "androidAnnotationProcessor", "androidKapt", "androidKsp1"])
    fun `Execute test task correctly`(resourcePath: String) {
        val dir: File
        GradleRunner.create()
            .withProjectDirFromResources(resourcePath)
            .also { dir = it.projectDir }
            .withPluginClasspath()
            .withArguments("test")
            .build()

        check(dir.walkBottomUp().none { it.extension == "lightsaber" })
    }
}
