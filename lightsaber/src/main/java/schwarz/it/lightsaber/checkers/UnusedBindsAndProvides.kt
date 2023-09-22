package schwarz.it.lightsaber.checkers

import dagger.model.BindingGraph
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.toCodePosition
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
    val componentsWithItsDeclaredModules = bindingGraph.getComponentsWithItsDeclaredModules(types)

    return componentsWithItsDeclaredModules.flatMap { (component, declaredModulesAtThisComponent) ->
        bindingGraph.getUsedModules()
            .filter { module -> declaredModulesAtThisComponent.contains(module) }
            .map { module -> module to (module.getBindings() - usedBindsAndProvides) }
            .flatMap { (module, unusedBindings) ->
                unusedBindings.map { binding ->
                    Finding("The $binding declared on `$module` is not used.", binding.getCodePosition())
                }
            }
    }
}

private fun BindingGraph.getComponentsWithItsDeclaredModules(
    types: Types,
): Map<BindingGraph.ComponentNode, List<Module>> {
    return componentNodes().associateWith { component ->
        component.getDeclaredModules(this, types).flatMap { it.toList() }
    }
}

private fun BindingGraph.getUsedBindsAndProvides(): Set<Module.Binding> {
    return bindings().filter { !it.contributingModule().isEmpty }.map { Module.Binding(it.bindingElement().get()) }
        .toSet()
}
