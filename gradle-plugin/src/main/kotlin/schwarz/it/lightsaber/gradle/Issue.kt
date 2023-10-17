package schwarz.it.lightsaber.gradle

internal sealed interface Issue {
    override fun toString(): String
}

internal class Error(
    private val rule: Rule,
    private val position: String,
    private val message: String,
) : Issue {
    override fun toString(): String {
        return "e: $position: $message [$rule]"
    }
}

internal class Warning(
    private val rule: Rule,
    private val position: String,
    private val message: String,
) : Issue {
    override fun toString(): String {
        return "w: $position: $message [$rule]"
    }
}

enum class Rule {
    EmptyComponent,
    UnusedBindInstance,
    UnusedBindsAndProvides,
    UnusedDependencies,
    UnusedModules,
}

internal fun String.toIssue(severities: Map<Rule, Severity>): Issue? {
    val groups = requireNotNull(regex.matchEntire(this)) { "Impossible to parse '$this'" }.groupValues
    val rule = try {
        Rule.valueOf(groups[4])
    } catch (ex: IllegalArgumentException) {
        throw IllegalArgumentException("Unknown rule '${groups[4]}'")
    }
    return when (severities[rule] ?: throw IllegalArgumentException("Unknown severity for rule '$rule'")) {
        Severity.Error -> Error(
            rule = rule,
            position = groups[1],
            message = groups[3],
        )

        Severity.Warning -> Warning(
            rule = rule,
            position = groups[1],
            message = groups[3],
        )

        Severity.Ignore -> null
    }
}

private val regex = """^(.+:[0-9]+(:[0-9]+)?): (.*) \[(.*)]$""".toRegex()
