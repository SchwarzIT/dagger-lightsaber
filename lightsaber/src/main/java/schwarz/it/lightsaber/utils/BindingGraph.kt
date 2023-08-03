package schwarz.it.lightsaber.utils

import dagger.model.BindingGraph
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
