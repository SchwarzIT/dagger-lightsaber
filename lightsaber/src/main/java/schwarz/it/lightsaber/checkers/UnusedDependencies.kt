package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import dagger.Component
import dagger.spi.model.BindingGraph
import dagger.spi.model.BindingKind
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.domain.Dependency
import schwarz.it.lightsaber.utils.fold
import schwarz.it.lightsaber.utils.getDeclaredArguments
import schwarz.it.lightsaber.utils.getDependenciesCodePosition
import schwarz.it.lightsaber.utils.getTypes
import schwarz.it.lightsaber.utils.getTypesMirrorsFromClass
import kotlin.jvm.optionals.getOrElse

internal fun checkUnusedDependencies(
    bindingGraph: BindingGraph,
    daggerProcessingEnv: DaggerProcessingEnv,
): List<Finding> {
    val used = bindingGraph.getUsedDependencies()
    return bindingGraph.componentNodes()
        .filterNot { it.isSubcomponent }
        .flatMap { component ->
            val declared = component.getDeclaredDependencies(daggerProcessingEnv)
            (declared - used).map {
                Finding(
                    "The dependency `$it` is not used.",
                    component.getDependenciesCodePosition(daggerProcessingEnv),
                )
            }
        }
}

private fun BindingGraph.getUsedDependencies(): Set<Dependency> {
    return bindings()
        .mapNotNull { binding ->
            when (binding.kind()) {
                BindingKind.COMPONENT_PROVISION -> {
                    binding.bindingElement()
                        .getOrElse { error("bindingElement() should never be empty in this context") }
                        .fold(
                            { element -> Dependency(element.enclosingElement) },
                            { ksAnnotated -> Dependency(ksAnnotated.parent!! as KSDeclaration) },
                        )
                }

                BindingKind.COMPONENT_DEPENDENCY -> {
                    Dependency(
                        binding.bindingElement()
                            .getOrElse { error("bindingElement() should never be empty in this context") },
                    )
                }

                else -> null
            }
        }
        .toSet()
}

private fun BindingGraph.ComponentNode.getDeclaredDependencies(daggerProcessingEnv: DaggerProcessingEnv): Set<Dependency> {
    return componentPath().currentComponent()
        .fold(
            { element ->
                element.getAnnotation(Component::class.java)
                    .getTypesMirrorsFromClass { dependencies }
                    .map { Dependency(daggerProcessingEnv.getTypes().asElement(it)) }
            },
            { classDeclaration: KSClassDeclaration ->
                classDeclaration
                    .getDeclaredArguments(Component::class, "dependencies")
                    .map { Dependency(it.declaration) }
            },
        )
        .toSet()
}
