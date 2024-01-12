package schwarz.it.lightsaber.checkers

import dagger.spi.model.BindingGraph
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.CodePosition
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.getCodePosition
import schwarz.it.lightsaber.toCodePosition
import schwarz.it.lightsaber.utils.fold
import schwarz.it.lightsaber.utils.getElements
import schwarz.it.lightsaber.utils.getFullQualifiedName
import schwarz.it.lightsaber.utils.getMethods
import schwarz.it.lightsaber.utils.getTypes
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types

internal fun checkEmptyComponents(
    bindingGraph: BindingGraph,
    daggerProcessingEnv: DaggerProcessingEnv,
): List<Finding> {
    return bindingGraph.componentNodes()
        .filter { componentNode ->
            componentNode.componentPath().currentComponent().fold(
                { it.getAllFunctions(daggerProcessingEnv.getTypes()).none() },
                { classDeclaration ->
                    val hasNoDeclaredFunctions = classDeclaration
                        .getAllFunctions()
                        .none { it.simpleName.asString() !in kspDefaultDeclaredFunctions }

                    val hasNoDeclaredProperties = classDeclaration
                        .getAllProperties()
                        .none()

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
                it,
            )
        }
}

private fun TypeElement.getAllFunctions(types: Types): Sequence<ExecutableElement> {
    return sequence {
        yieldAll(getMethods())
        yieldAll(interfaces.asSequence().flatMap { (types.asElement(it) as TypeElement).getAllFunctions(types) })
        yieldAll((types.asElement(superclass) as TypeElement?)?.getAllFunctions(types).orEmpty())
    }
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
