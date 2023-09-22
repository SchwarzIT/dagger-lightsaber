package schwarz.it.lightsaber.utils

import dagger.model.BindingGraph
import schwarz.it.lightsaber.CodePosition
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.jvm.optionals.getOrNull

internal fun BindingGraph.getUsedModules(): Set<Module> {
    return bindings()
        .asSequence()
        .mapNotNull { it.contributingModule().getOrNull() }
        .distinct()
        .flatMap { element ->
            if (element.isCompanionModule()) {
                listOf(element, element.enclosingElement as TypeElement)
            } else {
                listOf(element)
            }
        }
        .map { Module(it) }
        .toSet()
}

private fun Element.isCompanionModule(): Boolean {
    val parentElement = enclosingElement
    return parentElement.isAnnotatedWith(dagger.Module::class) &&
        parentElement is TypeElement &&
        simpleName.toString() == "Companion"
}

internal fun BindingGraph.ComponentNode.getModulesCodePosition(): CodePosition {
    val componentElement = componentPath().currentComponent()
    val annotationMirror = componentElement.findAnnotationMirrors("Component")
        ?: componentElement.findAnnotationMirrors("Subcomponent")!!
    return CodePosition(componentElement, annotationMirror, annotationMirror.getAnnotationValue("modules"))
}

internal fun BindingGraph.ComponentNode.getDependenciesCodePosition(): CodePosition {
    val componentElement = componentPath().currentComponent()
    val annotationMirror = componentElement.findAnnotationMirrors("Component")!!
    return CodePosition(componentElement, annotationMirror, annotationMirror.getAnnotationValue("dependencies"))
}
