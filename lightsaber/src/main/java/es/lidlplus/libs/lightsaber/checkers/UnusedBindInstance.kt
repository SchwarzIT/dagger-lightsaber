package es.lidlplus.libs.lightsaber.checkers

import dagger.BindsInstance
import dagger.Component
import dagger.Subcomponent
import dagger.model.BindingGraph
import dagger.model.BindingKind
import dagger.spi.DiagnosticReporter
import es.lidlplus.libs.lightsaber.ReportType
import es.lidlplus.libs.lightsaber.toKind
import es.lidlplus.libs.lightsaber.utils.isAnnotatedWith
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement

internal fun checkUnusedBindInstance(
    bindingGraph: BindingGraph,
    diagnosticReporter: DiagnosticReporter,
    reportType: ReportType,
) {
    if (reportType == ReportType.Ignore) return

    val usedInstances = bindingGraph.getUsedBindInstances()

    bindingGraph
        .componentNodes()
        .forEach { componentNode ->
            val bindInstances = componentNode.getBindInstances()

            (bindInstances - usedInstances).forEach {
                diagnosticReporter.reportComponent(
                    reportType.toKind(),
                    componentNode,
                    "The @BindsInstance `$it` is not used.",
                )
            }
        }
}

private fun Element.getMethods(): List<ExecutableElement> {
    return enclosedElements.filter { it.kind == ElementKind.METHOD }.mapNotNull { it as? ExecutableElement }
}

private fun BindingGraph.ComponentNode.getComponentFactory(): List<Element> {
    return componentPath()
        .currentComponent()
        .enclosedElements
        .filter { it.isAnnotatedWith(Component.Factory::class) || it.isAnnotatedWith(Subcomponent.Factory::class) }
}

private fun BindingGraph.ComponentNode.getBindInstances(): Set<Element> {
    val factory = getComponentFactory()
    return factory
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
