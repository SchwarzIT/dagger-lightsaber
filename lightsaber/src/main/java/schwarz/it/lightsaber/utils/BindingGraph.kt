package schwarz.it.lightsaber.utils

import dagger.model.BindingGraph
import schwarz.it.lightsaber.CodePosition
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.TypeElement
import kotlin.jvm.optionals.getOrNull

internal fun BindingGraph.getUsedModules(): Set<Module> {
    return bindings()
        .asSequence()
        .mapNotNull { it.contributingModule().getOrNull() }
        .distinct()
        .flatMap { element ->
            val parentElement = element.enclosingElement
            if (parentElement.isAnnotatedWith(dagger.Module::class) &&
                parentElement is TypeElement &&
                element.simpleName.toString() == "Companion"
            ) {
                listOf(element, parentElement)
            } else {
                listOf(element)
            }
        }
        .map { Module(it) }
        .toSet()
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
