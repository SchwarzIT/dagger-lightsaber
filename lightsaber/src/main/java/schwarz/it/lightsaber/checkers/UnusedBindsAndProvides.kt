package schwarz.it.lightsaber.checkers

import dagger.model.BindingGraph
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.utils.Module
import schwarz.it.lightsaber.utils.getDeclaredModules
import schwarz.it.lightsaber.utils.getUsedModules
import schwarz.it.lightsaber.utils.toList
import javax.lang.model.util.Types

internal fun checkUnusedBindsAndProvides(
    bindingGraph: BindingGraph,
    types: Types,
): List<Finding> {
    val usedBindsAndProvides = bindingGraph.getUsedBindsAndProvides()
    val allUsedModulesWithItsBindings = bindingGraph.getUsedModulesWithItsBindings()
    val componentsWithItsDeclaredModules = bindingGraph.getComponentsWithItsDeclaredModules(types)

    return componentsWithItsDeclaredModules.flatMap { (component, declaredModulesAtThisComponent) ->
        allUsedModulesWithItsBindings.getDeclaredAndUsedModulesWithItsBindings(declaredModulesAtThisComponent)
            .getModulesWithUnusedBindings(usedBindsAndProvides).flatMap { (module, unusedBindings) ->
                unusedBindings.map { binding ->
                    Finding(
                        component,
                        "The $binding declared on `$module` is not used.",
                    )
                }
            }
    }
}

private fun Map<Module, List<Module.Binding>>.getModulesWithUnusedBindings(usedBindsAndProvides: Set<Module.Binding>) =
    map { (module, declaredBindingsAtThisModule) -> module to (declaredBindingsAtThisModule - usedBindsAndProvides) }

private fun Map<Module, List<Module.Binding>>.getDeclaredAndUsedModulesWithItsBindings(
    declaredModulesAtThisComponent: List<Module>,
) = filter { (module, _) -> declaredModulesAtThisComponent.contains(module) }

private fun BindingGraph.getComponentsWithItsDeclaredModules(
    types: Types,
): Map<BindingGraph.ComponentNode, List<Module>> {
    return componentNodes().associateWith { component ->
        component.getDeclaredModules(this, types).flatMap { it.toList() }
    }
}

private fun BindingGraph.getUsedModulesWithItsBindings(): Map<Module, List<Module.Binding>> {
    return getUsedModules().associateWith { module -> module.getBindings() }
}

private fun BindingGraph.getUsedBindsAndProvides(): Set<Module.Binding> {
    return bindings().filter { !it.contributingModule().isEmpty }.map { Module.Binding(it.bindingElement().get()) }
        .toSet()
}
