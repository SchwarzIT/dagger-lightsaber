package schwarz.it.lightsaber.checkers

import dagger.BindsInstance
import dagger.Component
import dagger.Subcomponent
import dagger.model.BindingGraph
import dagger.model.BindingKind
import schwarz.it.lightsaber.Issue
import schwarz.it.lightsaber.ReportType
import schwarz.it.lightsaber.utils.isAnnotatedWith
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement

internal fun checkUnusedBindInstance(
    bindingGraph: BindingGraph,
    reportType: ReportType,
): List<Issue> {
    if (reportType == ReportType.Ignore) return emptyList()

    val usedInstances = bindingGraph.getUsedBindInstances()

    return bindingGraph
        .componentNodes()
        .flatMap { componentNode ->
            val bindInstances = componentNode.getBindInstances()

            (bindInstances - usedInstances).map {
                Issue(
                    componentNode,
                    "The @BindsInstance `$it` is not used.",
                )
            }
        }
}

private fun Element.getMethods(): List<ExecutableElement> {
    return enclosedElements.filter { it.kind == ElementKind.METHOD }.mapNotNull { it as? ExecutableElement }
}

private fun BindingGraph.ComponentNode.getComponentFactoriesAndBuilders(): List<Element> {
    return componentPath()
        .currentComponent()
        .enclosedElements
        .filter {
            it.isAnnotatedWith(Component.Factory::class) || it.isAnnotatedWith(Subcomponent.Factory::class) ||
                it.isAnnotatedWith(Component.Builder::class) || it.isAnnotatedWith(Subcomponent.Builder::class)
        }
}

private fun BindingGraph.ComponentNode.getBindInstances(): Set<Element> {
    val factoriesAndBuilders = getComponentFactoriesAndBuilders()
    return factoriesAndBuilders
        .flatMap { element ->
            element.getMethods()
                .flatMap { it.parameters }
                .filter { it.isAnnotatedWith(BindsInstance::class) }
        }
        .toSet()
}

private fun BindingGraph.getUsedBindInstances(): Set<Element> {
    return bindings()
        .filter { it.kind() == BindingKind.BOUND_INSTANCE }
        .map { it.bindingElement().get() }
        .toSet()
}
