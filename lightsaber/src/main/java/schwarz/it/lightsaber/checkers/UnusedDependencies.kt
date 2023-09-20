package schwarz.it.lightsaber.checkers

import dagger.model.BindingGraph
import dagger.model.BindingKind
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.utils.getComponentAnnotation
import schwarz.it.lightsaber.utils.getDependenciesCodePosition
import schwarz.it.lightsaber.utils.getTypesMirrorsFromClass
import javax.lang.model.element.Element
import javax.lang.model.util.Types
import kotlin.jvm.optionals.getOrElse

internal fun checkUnusedDependencies(
    bindingGraph: BindingGraph,
    types: Types,
): List<Finding> {
    val used = bindingGraph.getUsedDependencies()
    return bindingGraph.componentNodes()
        .filterNot { it.isSubcomponent }
        .flatMap { component ->
            val declared = component.getDeclaredDependencies(types)
            (declared - used).map {
                Finding("The dependency `$it` is not used.", component.getDependenciesCodePosition())
            }
        }
}

private fun BindingGraph.getUsedDependencies(): Set<Element> {
    return bindings()
        .mapNotNull { binding ->
            when (binding.kind()) {
                BindingKind.COMPONENT_PROVISION -> {
                    binding.bindingElement()
                        .getOrElse { error("bindingElement() should never be empty in this context") }
                        .enclosingElement
                }

                BindingKind.COMPONENT_DEPENDENCY -> {
                    binding.bindingElement()
                        .getOrElse { error("bindingElement() should never be empty in this context") }
                }

                else -> null
            }
        }
        .toSet()
}

private fun BindingGraph.ComponentNode.getDeclaredDependencies(types: Types): Set<Element> {
    return getComponentAnnotation().getTypesMirrorsFromClass { dependencies }
        .map { types.asElement(it) }.toSet()
}
