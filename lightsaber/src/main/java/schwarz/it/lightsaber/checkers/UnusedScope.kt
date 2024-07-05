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
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.util.Elements

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
                    classDeclaration::hasSuppress,
                )
            }
    }
}

internal class UnusedScopeJavac(
    private val elements: Elements,
) : LightsaberJavacRule {
    private val declarations: MutableList<Element> = mutableListOf()
    private val injects: MutableList<String> = mutableListOf()
    private val annotations: MutableSet<String> =
        mutableSetOf(Singleton::class.qualifiedName!!)

    override fun process(roundEnv: RoundEnvironment) {
        annotations.addAll(
            roundEnv.getElementsAnnotatedWith(Scope::class.java)
                .filter { it.kind == ElementKind.ANNOTATION_TYPE }
                .map { it.asType().toString() },
        )

        injects.addAll(
            roundEnv.getElementsAnnotatedWith(Inject::class.java)
                .filterIsInstance<ExecutableElement>()
                .map { it.enclosingElement.toString() },
        )

        declarations.addAll(
            roundEnv.getElementsAnnotatedWith(Scope::class.java)
                .map { it.asType().toString() }
                .plus(Singleton::class.qualifiedName!!)
                .flatMap { roundEnv.getElementsAnnotatedWith(elements.getTypeElement(it)) },
        )

    }

    override fun computeFindings(): List<Finding> {
        return declarations
            .filter { declaration -> !injects.contains(declaration.asType().toString()) }
            .map { classDeclaration ->
                val annotationName =
                    annotations.find { annotation ->
                        classDeclaration.annotationMirrors.any {
                            it.annotationType.toString() == annotation
                        }
                    }

                Finding(
                    "The `@$annotationName` scope is unused because `${classDeclaration}` doesn't contain any constructor annotated with `@Inject`.",
                    elements.getCodePosition(classDeclaration),
                    classDeclaration::hasSuppress
                )
            }
    }
}
