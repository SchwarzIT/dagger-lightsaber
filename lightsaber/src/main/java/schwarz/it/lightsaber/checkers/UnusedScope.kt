package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.outerType
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import dagger.Provides
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.LightsaberKspRule
import schwarz.it.lightsaber.toCodePosition
import javax.inject.Scope
import javax.inject.Singleton

internal class UnusedScopeKsp : LightsaberKspRule {

    private val declarations = mutableListOf<KSClassDeclaration>()
    private val provides = mutableListOf<KSAnnotated>()

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

        provides.addAll(
            resolver.getSymbolsWithAnnotation(Provides::class.qualifiedName!!),
        )
    }

    override fun computeFindings(): List<Finding> {
        val provide = provides.map {
            (it as KSFunctionDeclaration).returnType!!.resolve()
                .declaration.closestClassDeclaration()!!.qualifiedName!!.asString()
        }
        return declarations
            .filter { provide.contains(it.qualifiedName!!.asString()) }
            .map {
                Finding(
                    "The @Singleton scope is Unused.",
                    it.location.toCodePosition(),
                )
            }
    }
}
