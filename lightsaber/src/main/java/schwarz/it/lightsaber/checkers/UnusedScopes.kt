package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import dagger.spi.model.hasAnnotation
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.LightsaberJavacRule
import schwarz.it.lightsaber.LightsaberKspRule
import schwarz.it.lightsaber.domain.hasSuppress
import schwarz.it.lightsaber.getCodePosition
import schwarz.it.lightsaber.toCodePosition
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.inject.Scope
import javax.inject.Singleton
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.util.Elements

internal class UnusedScopesKsp : LightsaberKspRule {
    private val scopes: MutableSet<String> = mutableSetOf(Singleton::class.qualifiedName!!)
    private val declarations: MutableList<KSClassDeclaration> = mutableListOf()
    private val injects: MutableList<KSType> = mutableListOf()

    override fun process(resolver: Resolver) {
        scopes.addAll(
            resolver.getSymbolsWithAnnotation(Scope::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .map { it.qualifiedName!!.asString() },
        )

        declarations.addAll(
            scopes
                .flatMap { resolver.getSymbolsWithAnnotation(it) }
                .filterIsInstance<KSClassDeclaration>(),
        )

        injects.addAll(
            resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName!!)
                .filterIsInstance<KSFunctionDeclaration>()
                .map { it.returnType!!.resolve() },
        )
    }

    override fun computeFindings(): List<Finding> {
        return declarations
            .filterNot { it.asStarProjectedType() in injects }
            .map { classDeclaration ->
                val annotationName = scopes.find { classDeclaration.hasAnnotation(it) }
                Finding(
                    "The `@$annotationName` scope is unused because `${classDeclaration.qualifiedName!!.asString()}` doesn't contain any constructor annotated with `@Inject`.",
                    classDeclaration.location.toCodePosition(),
                    classDeclaration::hasSuppress,
                )
            }
    }
}

internal class UnusedScopesJavac(
    private val elements: Elements,
) : LightsaberJavacRule {
    private val scopes: MutableSet<String> = mutableSetOf(Singleton::class.qualifiedName!!)
    private val declarations: MutableList<Element> = mutableListOf()
    private val injects: MutableList<String> = mutableListOf()

    override fun process(roundEnv: RoundEnvironment) {
        scopes.addAll(
            roundEnv.getElementsAnnotatedWith(Scope::class.java)
                .map { it.asType().toString() },
        )

        declarations.addAll(
            scopes
                .flatMap { roundEnv.getElementsAnnotatedWith(elements.getTypeElement(it)) },
        )

        injects.addAll(
            roundEnv.getElementsAnnotatedWith(Inject::class.java)
                .filterIsInstance<ExecutableElement>()
                .map { it.enclosingElement.toString() },
        )
    }

    override fun computeFindings(): List<Finding> {
        return declarations
            .filterNot { it.asType().toString() in injects }
            .map { classDeclaration ->
                val annotationName = scopes.find { annotation ->
                    classDeclaration.annotationMirrors
                        .any { it.annotationType.toString() == annotation }
                }

                Finding(
                    "The `@$annotationName` scope is unused because `$classDeclaration` doesn't contain any constructor annotated with `@Inject`.",
                    elements.getCodePosition(classDeclaration),
                    classDeclaration::hasSuppress,
                )
            }
    }
}
