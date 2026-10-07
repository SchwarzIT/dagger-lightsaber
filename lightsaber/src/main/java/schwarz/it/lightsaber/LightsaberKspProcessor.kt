package schwarz.it.lightsaber

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import schwarz.it.lightsaber.checkers.UnusedInjectKsp
import schwarz.it.lightsaber.checkers.UnusedScopesKsp
import schwarz.it.lightsaber.utils.writeFile
import javax.tools.Diagnostic

internal class LightsaberKspProcessor(
    private val reporting: Reporting,
    private val logger: KSPLogger,
    private val config: AnnotationProcessorConfig,
) : SymbolProcessor {
    private val rules: Set<Pair<String, LightsaberKspRule>> = buildSet {
        if (config.checkUnusedInject) {
            add("UnusedInject" to UnusedInjectKsp())
        }
        if (config.checkUnusedScopes) {
            add("UnusedScopes" to UnusedScopesKsp())
        }
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        rules.forEach { (_, rule) -> rule.process(resolver) }

        return emptyList()
    }

    override fun finish() {
        val issues = rules
            .flatMap { (name, rule) ->
                rule.computeFindings()
                    .filterNot { it.suppression.hasSuppress(name) }
                    .map { Issue(it.codePosition, it.message, name) }
            }

        if (issues.isEmpty()) return
        when (reporting) {
            is Reporting.Files -> reporting.fileGenerator.writeFile("ksp", issues)

            is Reporting.Diagnostics -> reporting.report(issues) { kind, message ->
                when (kind) {
                    Diagnostic.Kind.ERROR -> logger.error(message)
                    else -> logger.warn(message)
                }
            }
        }
    }
}

interface LightsaberKspRule {
    fun process(resolver: Resolver)

    fun computeFindings(): List<Finding>
}

class LightsaberKspProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val reporting = environment.options.toReporting() ?: return NoOpSymbolProcessor
        val config = AnnotationProcessorConfig(
            checkUnusedInject = environment.options["Lightsaber.CheckUnusedInject"] != "false",
            checkUnusedScopes = environment.options["Lightsaber.CheckUnusedScopes"] != "false",
        )
        return LightsaberKspProcessor(reporting, environment.logger, config)
    }
}

private object NoOpSymbolProcessor : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        return emptyList()
    }
}

internal data class AnnotationProcessorConfig(
    val checkUnusedInject: Boolean,
    val checkUnusedScopes: Boolean,
)
