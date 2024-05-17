package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import dagger.Provides
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.Issue
import schwarz.it.lightsaber.LightsaberSymbolProcessorRule
import schwarz.it.lightsaber.toCodePosition
import javax.inject.Inject

internal class UnusedInject : LightsaberSymbolProcessorRule {
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

    override fun computeFindings(): List<Finding> {
        return injects
            .filter { it.returnType!!.resolve() in provides }
            .map { Finding("This Inject is unused", it.location.toCodePosition()) }
    }
}
