package es.lidlplus.libs.lightsaber.utils

import dagger.model.BindingGraph
import javax.lang.model.element.TypeElement
import kotlin.jvm.optionals.getOrNull
import dagger.Module

internal fun BindingGraph.getUsedModules(): Set<TypeElement> {
    return bindings()
        .asSequence()
        .mapNotNull { it.contributingModule().getOrNull() }
        .distinct()
        .flatMap { element ->
            val parentElement = element.enclosingElement
            if (parentElement.isAnnotatedWith(Module::class)
                && parentElement is TypeElement
                && element.simpleName.toString() == "Companion"
            ) {
                listOf(element, parentElement)
            } else {
                listOf(element)
            }
        }
        .toSet()
}
