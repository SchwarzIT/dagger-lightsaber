package schwarz.it.lightsaber.utils

import dagger.Component
import dagger.Subcomponent
import dagger.spi.model.BindingGraph
import schwarz.it.lightsaber.CodePosition
import schwarz.it.lightsaber.domain.FactoryOrBuilder
import schwarz.it.lightsaber.domain.Module
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.jvm.optionals.getOrNull

internal fun BindingGraph.getUsedModules(): Set<Module> {
    return bindings()
        .asSequence()
        .mapNotNull { it.contributingModule().getOrNull() }
        .distinct()
        .flatMap {
            it.fold(
                { element ->
                    if (element.isCompanionModule()) {
                        listOf(Module(element), Module(element.enclosingElement as TypeElement))
                    } else {
                        listOf(Module(element))
                    }
                },
                { TODO("ksp is not supported yet") },
            )
        }
        .toSet()
}

private fun Element.isCompanionModule(): Boolean {
    val parentElement = enclosingElement
    return parentElement.isAnnotatedWith(dagger.Module::class) &&
        parentElement is TypeElement &&
        simpleName.toString() == "Companion"
}

internal fun BindingGraph.ComponentNode.getModulesCodePosition(): CodePosition {
    return componentPath().currentComponent()
        .fold(
            { element ->
                val annotationMirror = element.findAnnotationMirrors("Component")
                    ?: element.findAnnotationMirrors("Subcomponent")!!
                CodePosition(
                    element,
                    annotationMirror,
                    annotationMirror.getAnnotationValue("modules"),
                )
            },
            { TODO("ksp is not supported yet") },
        )
}

internal fun BindingGraph.ComponentNode.getDependenciesCodePosition(): CodePosition {
    return componentPath().currentComponent()
        .fold(
            { element ->
                val annotationMirror = element.findAnnotationMirrors("Component")!!
                CodePosition(
                    element,
                    annotationMirror,
                    annotationMirror.getAnnotationValue("dependencies"),
                )
            },
            { TODO("ksp is not supported yet") },
        )
}

internal fun BindingGraph.ComponentNode.getComponentFactoriesAndBuilders(): List<FactoryOrBuilder> {
    return componentPath()
        .currentComponent()
        .fold(
            { element ->
                element.enclosedElements
                    .filter {
                        it.isAnnotatedWith(Component.Factory::class) || it.isAnnotatedWith(Subcomponent.Factory::class) ||
                            it.isAnnotatedWith(Component.Builder::class) || it.isAnnotatedWith(Subcomponent.Builder::class)
                    }
                    .map { FactoryOrBuilder(it) }
            },
            { TODO("ksp is not supported yet") },
        )
}
