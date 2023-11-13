package schwarz.it.lightsaber.gradle.truth

import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.TaskOutcome

fun assertThat(actual: BuildTask?): BuildTaskSubject {
    return Truth.assertAbout(BuildTaskSubject.Factory).that(actual)
}

class BuildTaskSubject(
    metadata: FailureMetadata,
    private val actual: BuildTask?,
) : Subject(metadata, actual) {

    fun hasOutcome(outcome: TaskOutcome) {
        check("getOutcome()").that(actual!!.outcome).isEqualTo(outcome)
    }

    internal object Factory : Subject.Factory<BuildTaskSubject, BuildTask> {
        override fun createSubject(metadata: FailureMetadata, actual: BuildTask?): BuildTaskSubject {
            return BuildTaskSubject(metadata, actual)
        }
    }
}
