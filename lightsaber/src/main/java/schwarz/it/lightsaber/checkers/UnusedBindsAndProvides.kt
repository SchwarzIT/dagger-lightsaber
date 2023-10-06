package schwarz.it.lightsaber.checkers

import dagger.spi.model.BindingGraph
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.domain.Module
import schwarz.it.lightsaber.utils.getDeclaredModules
import schwarz.it.lightsaber.utils.getUsedModules
import schwarz.it.lightsaber.utils.toList

internal fun checkUnusedBindsAndProvides(
    bindingGraph: BindingGraph,
    daggerProcessingEnv: DaggerProcessingEnv,
): List<Finding> {
    val usedBindsAndProvides = bindingGraph.getUsedBindsAndProvides()
    val componentsWithItsDeclaredModules =
        bindingGraph.getComponentsWithItsDeclaredModules(daggerProcessingEnv)

    return componentsWithItsDeclaredModules.flatMap { (component, declaredModulesAtThisComponent) ->
        bindingGraph.getUsedModules()
            .filter { module -> declaredModulesAtThisComponent.contains(module) }
            .map { module -> module to (module.getBindings() - usedBindsAndProvides) }
            .flatMap { (module, unusedBindings) ->
                unusedBindings.map { binding ->
                    Finding(
                        "The $binding declared on `$module` is not used.",
                        binding.getCodePosition(daggerProcessingEnv),
                    )
                }
            }
    }
}

private fun BindingGraph.getComponentsWithItsDeclaredModules(
    daggerProcessingEnv: DaggerProcessingEnv,
): Map<BindingGraph.ComponentNode, List<Module>> {
    return componentNodes().associateWith { component ->
        component
            .getDeclaredModules(this, daggerProcessingEnv)
            .flatMap { it.toList() }
    }
}

private fun BindingGraph.getUsedBindsAndProvides(): Set<Module.Binding> {
    return bindings()
        .filter { !it.contributingModule().isEmpty }
        .map { Module.Binding(it.bindingElement().get()) }
        .toSet()
}
