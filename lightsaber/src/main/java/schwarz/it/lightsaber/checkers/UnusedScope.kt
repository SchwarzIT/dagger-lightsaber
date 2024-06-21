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
import schwarz.it.lightsaber.domain.hasSuppress
import schwarz.it.lightsaber.toCodePosition
import javax.inject.Inject
import javax.inject.Scope
import javax.inject.Singleton
import kotlin.reflect.KClass

internal class UnusedScopeKsp : LightsaberKspRule {

    private val declarations = mutableListOf<KSClassDeclaration>()
    private val annotations = mutableSetOf(Singleton::class.qualifiedName!!)
    private val injects = mutableListOf<KSType>()

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

        injects.addAll(
            resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName!!)
                .filterIsInstance<KSFunctionDeclaration>()
                .map { it.returnType!!.resolve() },
        )

        annotations.addAll(
            resolver.getSymbolsWithAnnotation(Scope::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .map { it.qualifiedName!!.asString() },
        )
    }

    override fun computeFindings(): List<Finding> {
        return declarations
            .filter { !injects.contains(it.asStarProjectedType()) }
            .map { classDeclaration ->
                val annotationName =
                    annotations.find { classDeclaration.hasAnnotation(it) }
                Finding(
                    "The `@$annotationName` scope is unused because `${classDeclaration.qualifiedName!!.asString()}` doesn't contain any constructor annotated with `@Inject`.",
                    classDeclaration.location.toCodePosition(),
                )
            }
    }
}
