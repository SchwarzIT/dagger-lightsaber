package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.outerType
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import dagger.Provides
import dagger.spi.model.hasAnnotation
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.LightsaberKspRule
import schwarz.it.lightsaber.toCodePosition
import javax.inject.Scope
import javax.inject.Singleton
import kotlin.reflect.KClass

internal class UnusedScopeKsp : LightsaberKspRule {

    private val declarations = mutableListOf<KSClassDeclaration>()
    private val provides = mutableListOf<KSAnnotated>()
    private val annotations = mutableListOf<KSClassDeclaration>()

    @OptIn(KspExperimental::class)
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

        /*  annotations.addAll(
              resolver
                  .getSymbolsWithAnnotation(Scope::class.qualifiedName!!)
                  .plus(resolver.getSymbolsWithAnnotation(Singleton::class.qualifiedName!!))
          )*/
        annotations.addAll(
            resolver.getSymbolsWithAnnotation(Scope::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .plus(Singleton::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>(),
        )
    }

    override fun computeFindings(): List<Finding> {
        val provide = provides.map {
            (it as KSFunctionDeclaration).returnType!!.resolve()
                .declaration.closestClassDeclaration()!!.qualifiedName!!.asString()
        }
        return declarations
            .filter { provide.contains(it.qualifiedName!!.asString()) }
            .map { classDeclaration ->
                val annotationName =
                    annotations.find { classDeclaration.hasAnnotation(it.qualifiedName!!.asString()) }
                Finding(
                    "The @$annotationName scope is unused.",
                    classDeclaration.location.toCodePosition(),
                )
            }
    }
}
