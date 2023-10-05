@file:OptIn(KspExperimental::class)

package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import dagger.Component
import dagger.spi.model.BindingGraph
import dagger.spi.model.BindingKind
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.domain.Dependency
import schwarz.it.lightsaber.utils.fold
import schwarz.it.lightsaber.utils.getDependenciesCodePosition
import schwarz.it.lightsaber.utils.getTypesMirrorsFromClass
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.jvm.optionals.getOrElse

internal fun checkUnusedDependencies(
    bindingGraph: BindingGraph,
    types: Types,
    elements: Elements,
): List<Finding> {
    val used = bindingGraph.getUsedDependencies()
    return bindingGraph.componentNodes()
        .filterNot { it.isSubcomponent }
        .flatMap { component ->
            val declared = component.getDeclaredDependencies(types)
            (declared - used).map {
                Finding("The dependency `$it` is not used.", component.getDependenciesCodePosition(elements))
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

private fun BindingGraph.ComponentNode.getDeclaredDependencies(types: Types): Set<Dependency> {
    return componentPath().currentComponent()
        .fold(
            { element ->
                element.getAnnotation(Component::class.java)
                    .getTypesMirrorsFromClass { dependencies }
                    .map { Dependency(types.asElement(it)) }
            },
            { classDeclaration: KSClassDeclaration ->
                classDeclaration.annotations
                    .single()
                    .arguments
                    .single { it.name?.getShortName() == "dependencies" }
                    .let { it.value as List<*> }
                    .map { Dependency((it as KSType).declaration) }
            },
        )
        .toSet()
}
