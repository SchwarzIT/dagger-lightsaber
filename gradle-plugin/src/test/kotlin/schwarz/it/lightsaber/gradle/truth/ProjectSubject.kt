package schwarz.it.lightsaber.gradle.truth

import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import org.gradle.api.Project

fun assertThat(actual: Project?): ProjectSubject {
    return Truth.assertAbout(ProjectSubject.Factory).that(actual)
}

class ProjectSubject(
    metadata: FailureMetadata,
    private val actual: Project?,
) : Subject(metadata, actual) {
    fun hasTask(taskName: String): TaskSubject {
        actual!!

        val task = actual.tasks.findByName(taskName)

        if (task == null) {
            failWithActual(Fact.simpleFact("The task $taskName doesn't exist"))
            error("WTF?") // this shouldn't be called
        }

        return assertThat(task)
    }

    fun doesntHaveTask(taskName: String) {
        actual!!

        if (actual.tasks.findByName(taskName) != null) {
            failWithActual(Fact.simpleFact("The task $taskName shouldn't exist"))
            error("WTF?") // this shouldn't be called
        }
    }

    internal object Factory : Subject.Factory<ProjectSubject, Project> {
        override fun createSubject(metadata: FailureMetadata, actual: Project?): ProjectSubject {
            return ProjectSubject(metadata, actual)
        }
    }
}
