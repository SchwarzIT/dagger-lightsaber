package schwarz.it.lightsaber

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import dagger.Provides
import java.io.PrintWriter
import javax.inject.Inject

class LightsaberSymbolProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    private val injects: MutableList<KSFunctionDeclaration> = mutableListOf()
    private val provides: MutableSet<KSType> = mutableSetOf()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        injects.addAll(
            resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName!!)
                .filterIsInstance<KSFunctionDeclaration>(),
        )
        provides.addAll(
            resolver.getSymbolsWithAnnotation(Provides::class.qualifiedName!!)
                .filterIsInstance<KSFunctionDeclaration>()
                .map { it.returnType!!.resolve() },
        )

        return emptyList()
    }

    override fun finish() {
        val issues = injects
            .filter { it.returnType!!.resolve() in provides }
            .ifEmpty { return }
            .map { Finding("This Inject is unused", it.location.toCodePosition()) }
            .map { Issue(it.codePosition, it.message, "UnusedInject") }

        codeGenerator.createNewFile(Dependencies.ALL_FILES, "", "ksp", ".lightsaber")
            .let(::PrintWriter)
            .use { writer ->
                issues.forEach { writer.println(it.getMessage()) }
            }
    }
}
