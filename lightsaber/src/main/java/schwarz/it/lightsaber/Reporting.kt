package schwarz.it.lightsaber

import schwarz.it.lightsaber.utils.FileGenerator
import javax.tools.Diagnostic
import kotlin.io.path.Path

internal const val PATH_OPTION = "Lightsaber.path"
internal const val REPORT_OPTION = "Lightsaber.Report"
internal const val SEVERITY_OPTION_PREFIX = "Lightsaber.Severity."
internal const val DIAGNOSTICS_REPORT = "diagnostics"

internal val RULE_NAMES = listOf(
    "EmptyComponents",
    "UnusedBindsInstances",
    "UnusedBindsAndProvides",
    "UnusedDependencies",
    "UnusedInject",
    "UnusedMembersInjectionMethods",
    "UnusedModules",
    "UnusedScopes",
)

internal val REPORTING_OPTIONS = setOf(PATH_OPTION, REPORT_OPTION) + RULE_NAMES.map { "$SEVERITY_OPTION_PREFIX$it" }

internal enum class Severity {
    Error,
    Warning,
    Ignore,
}

internal sealed interface Reporting {
    class Files(val fileGenerator: FileGenerator) : Reporting

    class Diagnostics(private val severities: Map<String, Severity>) : Reporting {
        fun report(issues: List<Issue>, sink: (Diagnostic.Kind, String) -> Unit) {
            issues.forEach { issue ->
                when (severities[issue.rule] ?: Severity.Error) {
                    Severity.Error -> sink(Diagnostic.Kind.ERROR, issue.toDiagnosticMessage())
                    Severity.Warning -> sink(Diagnostic.Kind.WARNING, issue.toDiagnosticMessage())
                    Severity.Ignore -> Unit
                }
            }
        }
    }
}

internal fun Map<String, String>.toReporting(): Reporting? {
    if (this[REPORT_OPTION] == DIAGNOSTICS_REPORT) {
        return Reporting.Diagnostics(toSeverities())
    }
    val path = this[PATH_OPTION] ?: return null
    return Reporting.Files(FileGenerator(Path(path)))
}

private fun Map<String, String>.toSeverities(): Map<String, Severity> {
    return filterKeys { it.startsWith(SEVERITY_OPTION_PREFIX) }
        .mapKeys { it.key.removePrefix(SEVERITY_OPTION_PREFIX) }
        .mapValues { (rule, value) ->
            Severity.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException(
                    "Invalid severity '$value' for Lightsaber rule '$rule'. Expected one of: error, warning, ignore",
                )
        }
}

private fun Issue.toDiagnosticMessage() = "$codePosition: $message [$rule]"
