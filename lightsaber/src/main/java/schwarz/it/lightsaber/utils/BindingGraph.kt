package schwarz.it.lightsaber.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dagger.Component
import dagger.Subcomponent
import dagger.spi.model.BindingGraph
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.CodePosition
import schwarz.it.lightsaber.domain.FactoryOrBuilder
import schwarz.it.lightsaber.domain.Module
import schwarz.it.lightsaber.getCodePosition
import schwarz.it.lightsaber.toCodePosition
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.jvm.optionals.getOrNull

internal fun BindingGraph.getUsedModules(): Set<Module> {
    return bindings()
        .asSequence()
        .mapNotNull { it.contributingModule().getOrNull() }
        .distinct()
        .map {
            it.fold(
                { element ->
                    if (element.isCompanionModule()) {
                        Module(element.enclosingElement as TypeElement)
                    } else {
                        Module(element)
                    }
                },
                { classDeclaration ->
                    if (classDeclaration.isCompanionObject) {
                        Module(classDeclaration.parent as KSClassDeclaration)
                    } else {
                        Module(classDeclaration)
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

internal fun BindingGraph.ComponentNode.getModulesCodePosition(daggerProcessingEnv: DaggerProcessingEnv): CodePosition {
    return componentPath().currentComponent()
        .fold(
            { element ->
                val annotationMirror = element.findAnnotationMirrors("Component")
                    ?: element.findAnnotationMirrors("Subcomponent")!!
                daggerProcessingEnv.getElements().getCodePosition(
                    element,
                    annotationMirror,
                    annotationMirror.getAnnotationValue("modules"),
                )
            },
            { classDeclaration ->
                classDeclaration.annotations
                    .single { it.shortName.asString() == "Component" || it.shortName.asString() == "Subcomponent" }
                    .location
                    .toCodePosition()
            },
        )
}

internal fun BindingGraph.ComponentNode.getDependenciesCodePosition(daggerProcessingEnv: DaggerProcessingEnv): CodePosition {
    return componentPath().currentComponent()
        .fold(
            { element ->
                val annotationMirror = element.findAnnotationMirrors("Component")!!
                daggerProcessingEnv.getElements().getCodePosition(
                    element,
                    annotationMirror,
                    annotationMirror.getAnnotationValue("dependencies"),
                )
            },
            { classDeclaration ->
                classDeclaration.annotations
                    .single { it.shortName.asString() == "Component" }
                    .location
                    .toCodePosition()
            },
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
                        factoryOrBuilderAnnotations.any { annotation ->
                            it.isAnnotatedWith(annotation)
                        }
                    }
                    .map { FactoryOrBuilder(it) }
            },
            { ksClassDeclaration ->
                ksClassDeclaration.declarations
                    .filter {
                        factoryOrBuilderAnnotations.any { annotation ->
                            it.isAnnotationPresent(annotation)
                        }
                    }
                    .map { FactoryOrBuilder(it as KSClassDeclaration) }
                    .toList()
            },
        )
}

private val factoryOrBuilderAnnotations = setOf(
    Component.Factory::class,
    Subcomponent.Factory::class,
    Component.Builder::class,
    Subcomponent.Builder::class,
)

internal fun BindingGraph.getQualifiedName(): String {
    return rootComponentNode().componentPath().currentComponent()
        .fold({ it.qualifiedName.toString() }, { it.qualifiedName!!.asString() })
}
