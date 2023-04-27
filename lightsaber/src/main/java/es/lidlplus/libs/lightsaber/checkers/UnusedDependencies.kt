package es.lidlplus.libs.lightsaber.checkers

import dagger.model.BindingGraph
import dagger.model.BindingKind
import dagger.spi.DiagnosticReporter
import es.lidlplus.libs.lightsaber.ReportType
import es.lidlplus.libs.lightsaber.toKind
import es.lidlplus.libs.lightsaber.utils.getComponentAnnotation
import es.lidlplus.libs.lightsaber.utils.getTypesMirrorsFromClass
import javax.lang.model.element.Element
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import kotlin.jvm.optionals.getOrElse

internal fun checkUnusedDependencies(
    bindingGraph: BindingGraph,
    diagnosticReporter: DiagnosticReporter,
    types: Types,
    reportType: ReportType
) {
    if (reportType == ReportType.Ignore) return

    val used = bindingGraph.getUsedDependencies()

    bindingGraph.componentNodes()
        .filterNot { it.isSubcomponent }
        .forEach { component ->
            val declared = component.getDeclaredDependencies(types)

            (declared - used).forEach {
                diagnosticReporter.reportComponent(
                    reportType.toKind(),
                    component,
                    "The dependency `$it` is not used.",
                )
            }
        }
}

private fun BindingGraph.getUsedDependencies(): Set<Element> {
    return bindings()
        .mapNotNull { binding ->
            when (binding.kind()) {
                BindingKind.COMPONENT_PROVISION -> {
                    binding.bindingElement()
                        .getOrElse { error("bindingElement() should never be empty in this context") }
                        .enclosingElement
                }
                BindingKind.COMPONENT_DEPENDENCY -> {
                    binding.bindingElement()
                        .getOrElse { error("bindingElement() should never be empty in this context") }
                }
                else -> null
            }
        }
        .toSet()
}

private fun BindingGraph.ComponentNode.getDeclaredDependencies(types: Types): Set<Element> {
    return getComponentAnnotation().getTypesMirrorsFromClass { dependencies }.map { types.asElement(it) }.toSet()
}
