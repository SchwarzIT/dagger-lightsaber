package schwarz.it.lightsaber.gradle.truth

import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import org.gradle.api.Task
import org.gradle.internal.impldep.org.bouncycastle.asn1.x500.style.RFC4519Style.description

fun assertThat(actual: Task?): TaskSubject {
    return Truth.assertAbout(TaskSubject.Factory).that(actual)
}

class TaskSubject(metadata: FailureMetadata, private val actual: Task?) : Subject(metadata, actual) {
    fun dependsOn(taskName: String): TaskSubject {
        actual!!

        val taskDependencies = actual.taskDependencies.getDependencies(actual).map { it.name }

        check("getTaskDependencies()").that(taskDependencies).contains(taskName)

        return assertThat(actual.project.tasks.getByName(taskName))
    }

    fun hasDescription(description: String): TaskSubject {
        actual!!

        check("getDescription()").that(actual.description).isEqualTo(description)

        return this
    }

    fun dependsExactlyOn(vararg taskName: String) {
        actual!!

        val taskDependencies = actual.taskDependencies.getDependencies(actual).map { it.name }

        check("getTaskDependencies()").that(taskDependencies).containsExactly(*taskName)
    }

    internal object Factory : Subject.Factory<TaskSubject, Task> {
        override fun createSubject(metadata: FailureMetadata, actual: Task?): TaskSubject {
            return TaskSubject(metadata, actual)
        }
    }
}
