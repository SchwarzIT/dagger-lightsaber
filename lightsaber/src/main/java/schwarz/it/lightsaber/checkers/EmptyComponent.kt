package schwarz.it.lightsaber.checkers

import dagger.spi.model.BindingGraph
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.CodePosition
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.getCodePosition
import schwarz.it.lightsaber.toCodePosition
import schwarz.it.lightsaber.utils.fold
import schwarz.it.lightsaber.utils.getElements
import schwarz.it.lightsaber.utils.getMethods

internal fun checkEmptyComponent(
    bindingGraph: BindingGraph,
    daggerProcessingEnv: DaggerProcessingEnv,
): List<Finding> {
    return bindingGraph.componentNodes()
        .filter { componentNode ->
            componentNode.componentPath().currentComponent().fold(
                {
                    it.getMethods().isEmpty()
                },
                { classDeclaration ->
                    val hasNoDeclaredFunctions = classDeclaration
                        .getAllFunctions()
                        .filter { it.simpleName.asString() !in kspDefaultDeclaredFunctions }
                        .toList()
                        .isEmpty()

                    val hasNoDeclaredProperties = classDeclaration
                        .getAllProperties()
                        .toList()
                        .isEmpty()

                    hasNoDeclaredFunctions && hasNoDeclaredProperties
                },
            )
        }
        .map {
            val annotation = if (it.isSubcomponent) {
                "@Subcomponent"
            } else {
                "@Component"
            }

            Finding(
                "The $annotation `${it.getFullQualifiedName()}` is empty and could be removed.",
                it.getCodePosition(daggerProcessingEnv),
            )
        }
}

private fun BindingGraph.ComponentNode.getFullQualifiedName(): String {
    return this.componentPath().currentComponent()
        .fold(
            { it.qualifiedName.toString() },
            { it.qualifiedName!!.asString() },
        )
}

private val kspDefaultDeclaredFunctions = listOf(
    "equals",
    "hashCode",
    "toString",
)

private fun BindingGraph.ComponentNode.getCodePosition(daggerProcessingEnv: DaggerProcessingEnv): CodePosition {
    return componentPath().currentComponent().fold(
        {
            daggerProcessingEnv.getElements().getCodePosition(it)
        },
        {
            it.location.toCodePosition()
        },
    )
}
