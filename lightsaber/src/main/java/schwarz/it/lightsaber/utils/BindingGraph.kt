package schwarz.it.lightsaber.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dagger.Component
import dagger.Subcomponent
import dagger.spi.model.BindingGraph
import schwarz.it.lightsaber.CodePosition
import schwarz.it.lightsaber.domain.FactoryOrBuilder
import schwarz.it.lightsaber.domain.Module
import schwarz.it.lightsaber.getCodePosition
import schwarz.it.lightsaber.toCodePosition
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
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
                { classDeclaration ->
                    if (classDeclaration.isCompanionObject) {
                        listOf(
                            Module(classDeclaration),
                            Module(classDeclaration.parent as KSClassDeclaration),
                        )
                    } else {
                        listOf(Module(classDeclaration))
                    }
                },
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

internal fun BindingGraph.ComponentNode.getModulesCodePosition(elements: Elements): CodePosition {
    return componentPath().currentComponent()
        .fold(
            { element ->
                val annotationMirror = element.findAnnotationMirrors("Component")
                    ?: element.findAnnotationMirrors("Subcomponent")!!
                elements.getCodePosition(
                    element,
                    annotationMirror,
                    annotationMirror.getAnnotationValue("modules"),
                )
            },
            { it.location.toCodePosition() },
        )
}

internal fun BindingGraph.ComponentNode.getDependenciesCodePosition(elements: Elements): CodePosition {
    return componentPath().currentComponent()
        .fold(
            { element ->
                val annotationMirror = element.findAnnotationMirrors("Component")!!
                elements.getCodePosition(
                    element,
                    annotationMirror,
                    annotationMirror.getAnnotationValue("dependencies"),
                )
            },
            { it.location.toCodePosition() },
        )
}

@OptIn(KspExperimental::class)
internal fun BindingGraph.ComponentNode.getComponentFactoriesAndBuilders(): List<FactoryOrBuilder> {
    return componentPath()
        .currentComponent()
        .fold(
            { element ->
                element.enclosedElements
                    .filter {
                        it.isAnnotatedWith(Component.Factory::class) ||
                            it.isAnnotatedWith(Subcomponent.Factory::class) ||
                            it.isAnnotatedWith(Component.Builder::class) ||
                            it.isAnnotatedWith(Subcomponent.Builder::class)
                    }
                    .map { FactoryOrBuilder(it) }
            },
            { ksClassDeclaration ->
                ksClassDeclaration.declarations
                    .filter {
                        it.isAnnotationPresent(Component.Factory::class) ||
                            it.isAnnotationPresent(Subcomponent.Factory::class) ||
                            it.isAnnotationPresent(Component.Builder::class) ||
                            it.isAnnotationPresent(Subcomponent.Builder::class)
                    }
                    .map { FactoryOrBuilder(it as KSClassDeclaration) }
                    .toList()
            },
        )
}

internal fun BindingGraph.getQualifiedName(): String {
    return rootComponentNode().componentPath().currentComponent()
        .fold({ it.qualifiedName.toString() }, { it.qualifiedName!!.asString() })
}
