@file:Suppress("UnstableApiUsage")

package schwarz.it.lightsaber.checkers

import com.google.common.graph.Network
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import dagger.spi.model.BindingGraph
import dagger.spi.model.DaggerProcessingEnv
import dagger.spi.model.DependencyRequest
import dagger.spi.model.RequestKind
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.getCodePosition
import schwarz.it.lightsaber.toCodePosition
import schwarz.it.lightsaber.utils.fold
import schwarz.it.lightsaber.utils.getElements
import schwarz.it.lightsaber.utils.getFullQualifiedName

internal fun checkUnusedMembersInjectionMethods(
    bindingGraph: BindingGraph,
    daggerProcessingEnv: DaggerProcessingEnv,
): List<Finding> {
    val network = bindingGraph.network()

    return bindingGraph.componentNodes().flatMap { component ->
        component.entryPoints()
            .filter { it.kind() == RequestKind.MEMBERS_INJECTION }
            .filter { entryPoint ->
                val depEdge = network.edges()
                    .filterIsInstance<BindingGraph.DependencyEdge>()
                    .single { it.dependencyRequest() == entryPoint }

                network.directChildren(network.incidentNodes(depEdge).target())
                    .isEmpty()
            }
            .map { entryPoint ->
                Finding(
                    "The members-injection method `${entryPoint.getMethodName()}` declared in `${component.getFullQualifiedName()}` is not used. " +
                        "`${entryPoint.key()}` doesn't have any variable or method annotated with @Inject.",
                    entryPoint.requestElement().get().fold(
                        { daggerProcessingEnv.getElements().getCodePosition(it) },
                        { it.location.toCodePosition() },
                    ),
                )
            }
    }
}

private fun <N: Any, E: Any> Network<N, E>.directChildren(target: N): List<N> {
    return this.adjacentNodes(target)
        .filter { hasEdgeConnecting(target, it) }
}

private fun DependencyRequest.getMethodName(): String {
    return this.requestElement().get().fold(
        { it.simpleName.toString() },
        { (it as KSFunctionDeclaration).simpleName.asString() },
    )
}
