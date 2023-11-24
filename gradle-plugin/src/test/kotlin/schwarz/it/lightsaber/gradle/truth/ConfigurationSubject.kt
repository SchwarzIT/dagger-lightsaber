package schwarz.it.lightsaber.gradle.truth

import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import org.gradle.api.artifacts.Configuration

fun assertThat(actual: Configuration?): ConfigurationSubject {
    return Truth.assertAbout(ConfigurationSubject.Factory).that(actual)
}

class ConfigurationSubject(metadata: FailureMetadata, private val actual: Configuration?) : Subject(metadata, actual) {
    fun contains(dependency: String) {
        actual!!

        val dependencies = actual.dependencies.map { "${it.group}:${it.name}" }

        check("getDependencies()").that(dependencies).contains(dependency)
    }

    internal object Factory : Subject.Factory<ConfigurationSubject, Configuration> {
        override fun createSubject(metadata: FailureMetadata, actual: Configuration?): ConfigurationSubject {
            return ConfigurationSubject(metadata, actual)
        }
    }
}
