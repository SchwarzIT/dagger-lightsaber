package schwarz.it.lightsaber.gradle

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IssueTest {

    private val severities = mapOf(
        Rule.UnusedBindInstance to Severity.Error,
        Rule.UnusedBindsAndProvides to Severity.Error,
        Rule.UnusedDependencies to Severity.Warning,
        Rule.UnusedModules to Severity.Ignore,
    )

    @Test
    fun testError() {
        val issue = "position:1:1: message [UnusedBindInstance]".toIssue(severities)

        assertThat(issue).isInstanceOf(Error::class.java)
        assertThat(issue.toString()).isEqualTo("e: position:1:1: message [UnusedBindInstance]")
    }

    @Test
    fun testWarning() {
        val issue = "position:1:1: message [UnusedDependencies]".toIssue(severities)

        assertThat(issue).isInstanceOf(Warning::class.java)
        assertThat(issue.toString()).isEqualTo("w: position:1:1: message [UnusedDependencies]")
    }

    @Test
    fun testIgnore() {
        val issue = "position:1:1: message [UnusedModules]".toIssue(severities)

        assertThat(issue).isNull()
    }

    @Test
    fun testInvalidIssue() {
        val e = assertThrows<IllegalArgumentException> { "position: message [UnusedModules]".toIssue(severities) }
        assertThat(e).hasMessageThat().isEqualTo("Impossible to parse 'position: message [UnusedModules]'")
    }

    @Test
    fun testUnknownRule() {
        val e = assertThrows<IllegalArgumentException> { "position:1:1: message [UnknownRule]".toIssue(severities) }
        assertThat(e).hasMessageThat().isEqualTo("Unknown rule 'UnknownRule'")
    }

    @Test
    fun testUnknownSeverity() {
        val e = assertThrows<IllegalArgumentException> { "position:1:1: message [UnusedModules]".toIssue(emptyMap()) }
        assertThat(e).hasMessageThat().isEqualTo("Unknown severity for rule 'UnusedModules'")
    }

    @Test
    fun testIssueWithoutColumn() {
        val issue = "position:1: message [UnusedBindInstance]".toIssue(severities)

        assertThat(issue).isInstanceOf(Error::class.java)
        assertThat(issue.toString()).isEqualTo("e: position:1: message [UnusedBindInstance]")
    }
}
