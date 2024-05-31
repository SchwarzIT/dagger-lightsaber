package schwarz.it.lightsaber

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import schwarz.it.lightsaber.checkers.UnusedInjectKsp
import java.io.PrintWriter

class LightsaberSymbolProcessor(
    private val codeGenerator: CodeGenerator,
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
            codeGenerator.createNewFile(Dependencies.ALL_FILES, "", "ksp", ".lightsaber")
                .let(::PrintWriter)
                .use { writer -> issues.forEach { writer.println(it.getMessage()) } }
        }
    }
}

interface LightsaberKspRule {
    fun process(resolver: Resolver): List<KSAnnotated>

    fun computeFindings(): List<Finding>
}
