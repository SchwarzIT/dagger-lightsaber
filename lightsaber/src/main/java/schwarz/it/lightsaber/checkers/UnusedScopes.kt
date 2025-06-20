package schwarz.it.lightsaber.checkers

import com.google.common.graph.ImmutableNetwork
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dagger.Component
import dagger.Subcomponent
import dagger.spi.model.Binding
import dagger.spi.model.BindingGraph
import dagger.spi.model.DaggerProcessingEnv
import dagger.spi.model.hasAnnotation
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.LightsaberJavacRule
import schwarz.it.lightsaber.LightsaberKspRule
import schwarz.it.lightsaber.domain.hasSuppress
import schwarz.it.lightsaber.getCodePosition
import schwarz.it.lightsaber.toCodePosition
import schwarz.it.lightsaber.utils.allSuccessors
import schwarz.it.lightsaber.utils.getScopeCodePosition
import schwarz.it.lightsaber.utils.isAnnotatedWith
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.inject.Scope
import javax.inject.Singleton
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.util.Elements
import kotlin.collections.filterNot
import kotlin.jvm.optionals.getOrNull

internal fun checkUnusedScopes(
    bindingGraph: BindingGraph,
    daggerProcessingEnv: DaggerProcessingEnv,
): List<Finding> {
    @Suppress("UnstableApiUsage")
    val network: ImmutableNetwork<BindingGraph.Node, BindingGraph.Edge> = bindingGraph.network()

    return bindingGraph.componentNodes()
        .flatMap { component -> component.scopes().map { scope -> component to scope } }
        .filter { (component, scope) ->
            network.allSuccessors(component)
                .filterIsInstance<Binding>()
                .none { it.scope().getOrNull() == scope }
        }
        .map { (component, scope) ->
            Finding(
                "The scope `$scope` on component `$component` is not used.",
                component.getScopeCodePosition(
                    daggerProcessingEnv,
                    scope.scopeAnnotation()!!.annotationTypeElement()!!.toString(),
                ),
                component.componentPath().currentComponent()::hasSuppress,
            )
        }
}

private val ignoreAnnotatedWith = listOf(
    Component::class.qualifiedName!!,
    Subcomponent::class.qualifiedName!!,
    "com.squareup.anvil.annotations.MergeComponent",
    "com.squareup.anvil.annotations.MergeSubcomponent",
)

internal class UnusedScopesKsp : LightsaberKspRule {
    private val scopes: MutableSet<String> = mutableSetOf(Singleton::class.qualifiedName!!)
    private val declarations: MutableList<Pair<KSClassDeclaration, String>> = mutableListOf()
    private val injects: MutableSet<String> = mutableSetOf()

    override fun process(resolver: Resolver) {
        scopes.addAll(
            resolver.getSymbolsWithAnnotation(Scope::class.qualifiedName!!)
                .filterIsInstance<KSClassDeclaration>()
                .map { it.qualifiedName!!.asString() },
        )

        declarations.addAll(
            scopes
                .asSequence()
                .flatMap { resolver.getSymbolsWithAnnotation(it) }
                .filterIsInstance<KSClassDeclaration>()
                .filterNot { declaration -> ignoreAnnotatedWith.any { declaration.hasAnnotation(it) } }
                .map { it to it.asStarProjectedType().declaration.qualifiedName!!.asString() },
        )

        injects.addAll(
            resolver.getSymbolsWithAnnotation(Inject::class.qualifiedName!!)
                .filterIsInstance<KSFunctionDeclaration>()
                .map { it.returnType!!.resolve().declaration.qualifiedName!!.asString() },
        )
    }

    override fun computeFindings(): List<Finding> {
        return declarations
            .filterNot { (_, type) -> type in injects }
            .map { (classDeclaration, _) ->
                val annotationName = scopes.find { classDeclaration.hasAnnotation(it) }
                Finding(
                    "The `@$annotationName` scope is unused because `${classDeclaration.qualifiedName!!.asString()}` doesn't contain any constructor annotated with `@Inject`.",
                    classDeclaration.annotations
                        .single { it.annotationType.resolve().declaration.qualifiedName!!.asString() == annotationName }
                        .location
                        .toCodePosition(),
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
                .asSequence()
                .flatMap { roundEnv.getElementsAnnotatedWith(elements.getTypeElement(it)) }
                .filterNot { element -> ignoreAnnotatedWith.any { element.isAnnotatedWith(it) } },
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
