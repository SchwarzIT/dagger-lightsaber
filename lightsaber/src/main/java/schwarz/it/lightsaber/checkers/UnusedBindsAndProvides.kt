package schwarz.it.lightsaber.checkers

import dagger.Binds
import dagger.Provides
import dagger.model.BindingGraph
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.utils.getDeclaredModules
import schwarz.it.lightsaber.utils.getUsedModules
import schwarz.it.lightsaber.utils.isAnnotatedWith
import schwarz.it.lightsaber.utils.toList
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types
import kotlin.reflect.KClass

internal fun checkUnusedBindsAndProvides(
    bindingGraph: BindingGraph,
    types: Types,
): List<Finding> {
    val usedBindsAndProvides = bindingGraph.getUsedBindsAndProvides()
    val allUsedModulesWithItsBindings = bindingGraph.getUsedModulesWithItsBindings()
    val componentsWithItsDeclaredModules = bindingGraph.getComponentsWithItsDeclaredModules(types)

    return componentsWithItsDeclaredModules.flatMap { (component, declaredModulesAtThisComponent) ->
        allUsedModulesWithItsBindings
            .getDeclaredAndUsedModulesWithItsBindings(declaredModulesAtThisComponent)
            .getModulesWithUnusedBindings(usedBindsAndProvides)
            .flatMap { (module, unusedBindings) ->
                unusedBindings.map { binding ->
                    Finding(
                        component,
                        "The @${binding.getBindingAnnotation().simpleName} `${binding.simpleName}` declared on `$module` is not used.",
                    )
                }
            }
    }
}

private fun Map<TypeElement, List<Element>>.getModulesWithUnusedBindings(usedBindsAndProvides: Set<Element>) =
    map { (module, declaredBindingsAtThisModule) -> module to (declaredBindingsAtThisModule - usedBindsAndProvides) }

private fun Map<TypeElement, List<Element>>.getDeclaredAndUsedModulesWithItsBindings(
    declaredModulesAtThisComponent: List<Element>,
) = filter { (module, _) -> declaredModulesAtThisComponent.contains(module) }

private fun BindingGraph.getComponentsWithItsDeclaredModules(
    types: Types,
): Map<BindingGraph.ComponentNode, List<Element>> {
    return componentNodes().associateWith { component ->
        component.getDeclaredModules(this, types)
            .flatMap { it.toList() }
    }
}

private fun BindingGraph.getUsedModulesWithItsBindings(): Map<TypeElement, List<Element>> {
    return getUsedModules().associateWith { module -> module.enclosedElements.filter { it.isABinding() } }
}

private fun BindingGraph.getUsedBindsAndProvides(): Set<Element> {
    return bindings()
        .filter { !it.contributingModule().isEmpty }
        .map { it.bindingElement().get() }
        .toSet()
}

private fun Element.getBindingAnnotation(): KClass<out Annotation> {
    return bindingAnnotations.first { isAnnotatedWith(it) }
}

private fun Element.isABinding(): Boolean {
    return bindingAnnotations.any { isAnnotatedWith(it) }
}

private val bindingAnnotations = listOf(Binds::class, Provides::class)
