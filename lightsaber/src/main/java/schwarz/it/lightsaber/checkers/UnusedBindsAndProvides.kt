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
    val declaredModules = bindingGraph.getDeclaredModules(daggerProcessingEnv)

    return bindingGraph.getUsedModules()
        .filter { module -> module in declaredModules }
        .associateWith { module -> module.getBindings() - usedBindsAndProvides }
        .flatMap { (module, unusedBindings) ->
            unusedBindings.map { binding ->
                Finding(
                    "The $binding declared in `$module` is not used.",
                    binding.getCodePosition(daggerProcessingEnv),
                    binding,
                )
            }
        }
}

private fun BindingGraph.getDeclaredModules(daggerProcessingEnv: DaggerProcessingEnv): Set<Module> {
    return componentNodes()
        .flatMap { component ->
            component
                .getDeclaredModules(this, daggerProcessingEnv)
                .flatMap { it.toList() }
        }
        .toSet()
}

private fun BindingGraph.getUsedBindsAndProvides(): Set<Module.Binding> {
    return bindings()
        .filter { !it.contributingModule().isEmpty }
        .map { Module.Binding(it.bindingElement().get()) }
        .toSet()
}
