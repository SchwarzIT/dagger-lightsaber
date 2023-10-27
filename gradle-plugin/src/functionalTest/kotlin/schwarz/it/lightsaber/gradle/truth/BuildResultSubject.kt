package schwarz.it.lightsaber.gradle.truth

import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import org.gradle.testkit.runner.BuildResult

fun assertThat(actual: BuildResult): BuildResultSubject {
    return Truth.assertAbout(BuildResultSubject.Factory).that(actual)
}

class BuildResultSubject(
    metadata: FailureMetadata,
    private val actual: BuildResult,
) : Subject(metadata, actual) {

    fun hasTask(taskPath: String): BuildTaskSubject {
        val tasks = actual.tasks.map { it.path }

        check("getTasks()").that(tasks).contains(taskPath)

        return assertThat(actual.task(taskPath)!!)
    }

    fun hasNotTask(taskPath: String) {
        val tasks = actual.tasks.map { it.path }

        check("getTasks()").that(tasks).doesNotContain(taskPath)
    }

    override fun actualCustomStringRepresentation(): String {
        return actual.output
    }

    fun contains(output: String) {
        check("getOutput()").that(actual.output).contains(output)
    }

    internal object Factory : Subject.Factory<BuildResultSubject, BuildResult> {
        override fun createSubject(failureMetadata: FailureMetadata, that: BuildResult): BuildResultSubject {
            return BuildResultSubject(failureMetadata, that)
        }
    }
}
