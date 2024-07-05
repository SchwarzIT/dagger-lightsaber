package schwarz.it.lightsaber

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import schwarz.it.lightsaber.checkers.UnusedInjectKsp
import schwarz.it.lightsaber.checkers.UnusedScopesKsp
import schwarz.it.lightsaber.utils.FileGenerator
import schwarz.it.lightsaber.utils.writeFile

internal class LightsaberKspProcessor(
    private val fileGenerator: FileGenerator,
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

        if (issues.isNotEmpty()) {
            fileGenerator.writeFile("ksp", issues)
        }
    }
}

interface LightsaberKspRule {
    fun process(resolver: Resolver)

    fun computeFindings(): List<Finding>
}

class LightsaberKspProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val config = AnnotationProcessorConfig(
            checkUnusedInject = environment.options["Lightsaber.CheckUnusedInject"] != "false",
            checkUnusedScopes = environment.options["Lightsaber.CheckUnusedScopes"] != "false",
        )
        return LightsaberKspProcessor(FileGenerator(environment.codeGenerator), config)
    }
}

internal data class AnnotationProcessorConfig(
    val checkUnusedInject: Boolean,
    val checkUnusedScopes: Boolean,
)
