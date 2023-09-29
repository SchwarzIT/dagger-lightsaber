package schwarz.it.lightsaber.checkers

import dagger.model.BindingGraph
import dagger.model.BindingKind
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.domain.FactoryOrBuilder
import schwarz.it.lightsaber.utils.getComponentFactoriesAndBuilders

internal fun checkUnusedBindInstance(
    bindingGraph: BindingGraph,
): List<Finding> {
    val usedInstances = bindingGraph.getUsedBindInstances()

    return bindingGraph
        .componentNodes()
        .flatMap { componentNode ->
            val bindInstances = componentNode.getBindInstances()

            (bindInstances - usedInstances).map {
                Finding("The @BindsInstance `$it` is not used.", it.getCodePosition())
            }
        }
}

private fun BindingGraph.ComponentNode.getBindInstances(): Set<FactoryOrBuilder.BindsInstance> {
    return getComponentFactoriesAndBuilders().flatMap { it.getBindInstance() }.toSet()
}

private fun BindingGraph.getUsedBindInstances(): Set<FactoryOrBuilder.BindsInstance> {
    return bindings()
        .filter { it.kind() == BindingKind.BOUND_INSTANCE }
        .map { FactoryOrBuilder.BindsInstance(it.bindingElement().get()) }
        .toSet()
}
