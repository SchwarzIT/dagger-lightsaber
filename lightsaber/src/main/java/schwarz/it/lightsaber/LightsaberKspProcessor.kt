package schwarz.it.lightsaber

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import schwarz.it.lightsaber.checkers.UnusedInjectKsp
import schwarz.it.lightsaber.utils.FileGenerator
import schwarz.it.lightsaber.utils.writeFile

internal class LightsaberKspProcessor(
    private val fileGenerator: FileGenerator,
    private val config: LightsaberConfig2,
) : SymbolProcessor {
    private val rules: Set<Pair<String, LightsaberKspRule>> = buildSet {
        if (config.checkUnusedInject) {
            add("UnusedInject" to UnusedInjectKsp())
        }
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        return rules
            .map { (_, rule) -> rule.process(resolver) }
            .flatten()
    }

    override fun finish() {
        val issues = rules
            .flatMap { (name, rule) -> rule.computeFindings().map { Issue(it.codePosition, it.message, name) } }

        if (issues.isNotEmpty()) {
            fileGenerator.writeFile( "ksp", issues)
        }
    }
}

interface LightsaberKspRule {
    fun process(resolver: Resolver): List<KSAnnotated>

    fun computeFindings(): List<Finding>
}

class LightsaberKspProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val config = LightsaberConfig2(
            checkUnusedInject = environment.options["Lightsaber.CheckUnusedInject"] != "false",
        )
        return LightsaberKspProcessor(FileGenerator(environment.codeGenerator), config)
    }
}

data class LightsaberConfig2(
    val checkUnusedInject: Boolean,
)
