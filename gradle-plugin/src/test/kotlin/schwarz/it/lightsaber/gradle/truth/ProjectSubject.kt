package schwarz.it.lightsaber.gradle.truth

import com.google.common.truth.Fact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

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

    fun doesntHasDependency(dependency: String) {
        actual!!
        val coordinates = dependency.split(":")

        val configuration = actual.configurations.find { configuration ->
            configuration.dependencies.any { it.hasCoordinates(coordinates[0], coordinates[1]) }
        }

        if (configuration != null) {
            failWithActual(Fact.simpleFact("The project has lightsaber dependency on the configuration ${configuration.name}"))
            error("WTF?") // this shouldn't be called
        }
    }

    fun hasConfiguration(name: String): ConfigurationSubject {
        actual!!

        val configuration = actual.configurations.findByName(name)

        if (configuration == null) {
            failWithActual(Fact.simpleFact("The configuration $name doesn't exist"))
            error("WTF?") // this shouldn't be called
        }

        return assertThat(configuration)
    }

    internal object Factory : Subject.Factory<ProjectSubject, Project> {
        override fun createSubject(metadata: FailureMetadata, actual: Project?): ProjectSubject {
            return ProjectSubject(metadata, actual)
        }
    }
}

private fun Dependency.hasCoordinates(group: String, name: String): Boolean {
    return this.group == group && this.name == name
}
