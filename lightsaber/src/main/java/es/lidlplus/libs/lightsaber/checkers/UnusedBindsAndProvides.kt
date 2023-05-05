package es.lidlplus.libs.lightsaber.checkers

import dagger.Binds
import dagger.Provides
import dagger.model.BindingGraph
import dagger.spi.DiagnosticReporter
import es.lidlplus.libs.lightsaber.ReportType
import es.lidlplus.libs.lightsaber.toKind
import es.lidlplus.libs.lightsaber.utils.TreeNode
import es.lidlplus.libs.lightsaber.utils.getDeclaredModules
import es.lidlplus.libs.lightsaber.utils.getUsedModules
import es.lidlplus.libs.lightsaber.utils.isAnnotatedWith
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types
import kotlin.reflect.KClass

internal fun checkUnusedBindsAndProvides(
    bindingGraph: BindingGraph,
    diagnosticReporter: DiagnosticReporter,
    types: Types,
    reportType: ReportType,
) {
    if (reportType == ReportType.Ignore) return

    val usedBindsAndProvides = bindingGraph.getUsedBindsAndProvides()
    val allUsedModulesWithItsBindings = bindingGraph.getUsedModulesWithItsBindings()
    val componentsWithItsDeclaredModules = bindingGraph.getComponentsWithItsDeclaredModules(types)

    componentsWithItsDeclaredModules.forEach { (component, declaredModulesAtThisComponent) ->
        allUsedModulesWithItsBindings
            .getDeclaredAndUsedModulesWithItsBindings(declaredModulesAtThisComponent)
            .getModulesWithUnusedBindings(usedBindsAndProvides)
            .forEach { (module, unusedBindings) ->
                unusedBindings.forEach { binding ->
                    diagnosticReporter.reportComponent(
                        reportType.toKind(),
                        component,
                        "The @${binding.getBindingAnnotation().simpleName} `${binding.simpleName}` declared on `$module` is not used.",
                    )
                }
            }
    }
}

private fun Map<TypeElement, List<Element>>.getModulesWithUnusedBindings(usedBindsAndProvides: Set<Element>) =
    map { (module, declaredBindingsAtThisModule) -> module to (declaredBindingsAtThisModule - usedBindsAndProvides) }

private fun Map<TypeElement, List<Element>>.getDeclaredAndUsedModulesWithItsBindings(
    declaredModulesAtThisComponent: List<Element>,
) = filter { (module, _) -> declaredModulesAtThisComponent.contains(module) }

private fun BindingGraph.getComponentsWithItsDeclaredModules(
    types: Types,
): Map<BindingGraph.ComponentNode, List<Element>> {
    return componentNodes().associateWith { component ->
        component.getDeclaredModules(this, types)
            .flatMap { node -> node.getAllNodes().map { it.value } }
    }
}

private fun <T> TreeNode<T>.getAllNodes(): List<TreeNode<T>> {
    return children.flatMap { it.getAllNodes() }.plus(this)
}

private fun BindingGraph.getUsedModulesWithItsBindings(): Map<TypeElement, List<Element>> {
    return getUsedModules().associateWith { module -> module.enclosedElements.filter { it.isABinding() } }
}

private fun BindingGraph.getUsedBindsAndProvides(): Set<Element> {
    return bindings()
        .filter { !it.contributingModule().isEmpty }
        .map { it.bindingElement().get() }
        .toSet()
}

private fun Element.getBindingAnnotation(): KClass<out Annotation> {
    return bindingAnnotations.first { isAnnotatedWith(it) }
}

private fun Element.isABinding(): Boolean {
    return bindingAnnotations.any { isAnnotatedWith(it) }
}

private val bindingAnnotations = listOf(Binds::class, Provides::class)
