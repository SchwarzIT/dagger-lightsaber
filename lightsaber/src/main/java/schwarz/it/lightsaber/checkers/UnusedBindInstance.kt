package schwarz.it.lightsaber.checkers

import dagger.spi.model.BindingGraph
import dagger.spi.model.BindingKind
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.domain.FactoryOrBuilder
import schwarz.it.lightsaber.utils.getComponentFactoriesAndBuilders
import schwarz.it.lightsaber.utils.getFullQualifiedName

internal fun checkUnusedBindInstance(
    bindingGraph: BindingGraph,
    daggerProcessingEnv: DaggerProcessingEnv,
): List<Finding> {
    val usedInstances = bindingGraph.getUsedBindInstances()

    return bindingGraph
        .componentNodes()
        .flatMap { componentNode ->
            val bindInstances = componentNode.getBindInstances()

            (bindInstances - usedInstances).map {
                Finding(
                    "The @BindsInstance `$it` declared in `${componentNode.getFullQualifiedName()}` is not used.",
                    it.getCodePosition(daggerProcessingEnv),
                )
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
