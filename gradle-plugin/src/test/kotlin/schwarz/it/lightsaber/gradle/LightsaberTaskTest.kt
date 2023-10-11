package schwarz.it.lightsaber.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.api.GradleException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LightsaberTaskTest {
    private val severities = mapOf(
        Rule.UnusedBindInstance to Severity.Error,
        Rule.UnusedBindsAndProvides to Severity.Error,
        Rule.UnusedDependencies to Severity.Warning,
        Rule.UnusedModules to Severity.Ignore,
    )

    @Test
    fun errorWhenNoIssues() {
        val e = assertThrows<IllegalArgumentException> { checkIssues(emptyList(), severities, false) }
        assertThat(e).hasMessageThat().isEqualTo("The .lightsaber files should never be empty")
    }

    @Test
    fun failWithOneError() {
        val issues = listOf("position:1:1: message [UnusedBindInstance]")
        val e = assertThrows<GradleException> { checkIssues(issues, severities, false) }
        assertThat(e).hasMessageThat().isEqualTo("Analysis failed with 1 error")
    }

    @Test
    fun failWithOneErrorAndOneWarning() {
        val issues = listOf(
            "position:1:1: message [UnusedBindInstance]",
            "position:1:1: message [UnusedDependencies]",
        )
        val e = assertThrows<GradleException> { checkIssues(issues, severities, false) }
        assertThat(e).hasMessageThat().isEqualTo("Analysis failed with 2 issues (1 was an error)")
    }

    @Test
    fun failWithOneErrorAndTwoWarning() {
        val issues = listOf(
            "position:1:1: message [UnusedBindInstance]",
            "position:1:1: message [UnusedDependencies]",
            "position:1:2: message [UnusedDependencies]",
        )
        val e = assertThrows<GradleException> { checkIssues(issues, severities, false) }
        assertThat(e).hasMessageThat().isEqualTo("Analysis failed with 3 issues (1 was an error)")
    }

    @Test
    fun failWithTwoError() {
        val issues = listOf(
            "position:1:1: message [UnusedBindInstance]",
            "position:1:2: message [UnusedBindInstance]",
        )
        val e = assertThrows<GradleException> { checkIssues(issues, severities, false) }
        assertThat(e).hasMessageThat().isEqualTo("Analysis failed with 2 errors")
    }

    @Test
    fun failWithTwoErrorAndOneWarning() {
        val issues = listOf(
            "position:1:1: message [UnusedBindInstance]",
            "position:1:2: message [UnusedBindInstance]",
            "position:1:1: message [UnusedDependencies]",
        )
        val e = assertThrows<GradleException> { checkIssues(issues, severities, false) }
        assertThat(e).hasMessageThat().isEqualTo("Analysis failed with 3 issues (2 were errors)")
    }

    @Test
    fun failWithTwoErrorAndTwoWarnings() {
        val issues = listOf(
            "position:1:1: message [UnusedBindInstance]",
            "position:1:2: message [UnusedBindInstance]",
            "position:1:1: message [UnusedDependencies]",
            "position:1:2: message [UnusedDependencies]",
        )
        val e = assertThrows<GradleException> { checkIssues(issues, severities, false) }
        assertThat(e).hasMessageThat().isEqualTo("Analysis failed with 4 issues (2 were errors)")
    }

    @Test
    fun successWithOneWarning() {
        val issues = listOf("position:1:1: message [UnusedDependencies]")
        checkIssues(issues, severities, false)
    }

    @Test
    fun successWithTwoWarning() {
        val issues = listOf("position:1:1: message [UnusedDependencies]", "position:1:2: message [UnusedDependencies]")
        checkIssues(issues, severities, false)
    }
}
