package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.LightsaberKspRule
import schwarz.it.lightsaber.toCodePosition
import javax.inject.Scope
import javax.inject.Singleton

internal class UnusedScopeKsp : LightsaberKspRule {

    private val declarations = mutableListOf<KSClassDeclaration>()

    override fun process(resolver: Resolver) {
        declarations.addAll(
            resolver
                .getSymbolsWithAnnotation(Scope::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .map { it.qualifiedName!!.asString() }
                .plus(Singleton::class.qualifiedName!!)
                .flatMap { resolver.getSymbolsWithAnnotation(it) }
                .filterIsInstance<KSClassDeclaration>(),
        )
    }

    override fun computeFindings(): List<Finding> {
        return declarations.map {
            Finding(
                "The @Singleton scope is Unused.",
                it.location.toCodePosition(),
            )
        }
    }
}
