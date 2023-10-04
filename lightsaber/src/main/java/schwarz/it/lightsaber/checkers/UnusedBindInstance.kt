package schwarz.it.lightsaber.checkers

import dagger.spi.model.BindingGraph
import dagger.spi.model.BindingKind
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.domain.FactoryOrBuilder
import schwarz.it.lightsaber.utils.getComponentFactoriesAndBuilders
import javax.lang.model.util.Elements

internal fun checkUnusedBindInstance(
    bindingGraph: BindingGraph,
    elements: Elements,
): List<Finding> {
    val usedInstances = bindingGraph.getUsedBindInstances()

    return bindingGraph
        .componentNodes()
        .flatMap { componentNode ->
            val bindInstances = componentNode.getBindInstances()

            (bindInstances - usedInstances).map {
                Finding("The @BindsInstance `$it` is not used.", it.getCodePosition(elements))
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
