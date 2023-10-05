package schwarz.it.lightsaber.utils

import dagger.Component
import dagger.Subcomponent
import dagger.spi.model.BindingGraph
import dagger.spi.model.DaggerTypeElement
import schwarz.it.lightsaber.domain.Module
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types
import kotlin.reflect.KClass

internal fun BindingGraph.ComponentNode.getDeclaredModules(
    bindingGraph: BindingGraph,
    types: Types,
): List<TreeNode<Module>> {
    val usedModules = bindingGraph.getUsedModules()
    return if (isSubcomponent) {
        componentPath().currentComponent()
            .fold(
                { element ->
                    element.getAnnotation(Subcomponent::class.java)
                        .getTypesMirrorsFromClass { modules }
                        .map { Module(types.asElement(it) as TypeElement) }
                },
                { it.getDeclaredModules(dagger.Subcomponent::class, "modules") },
            )
    } else {
        componentPath().currentComponent()
            .fold(
                { element ->
                    element.getAnnotation(Component::class.java)
                        .getTypesMirrorsFromClass { modules }
                        .map { Module(types.asElement(it) as TypeElement) }
                },
                { it.getDeclaredModules(dagger.Component::class, "modules") },
            )
    }.map { module -> getModuleTree(usedModules, module, types) }
}

private fun getModuleTree(
    usedModules: Set<Module>,
    module: Module,
    types: Types,
): TreeNode<Module> {
    return TreeNode(
        value = module,
        children = module.getIncludedModules(types)
            .map { getModuleTree(usedModules, it, types) },
    )
}

class TreeNode<T>(val value: T, val children: List<TreeNode<T>> = emptyList())

internal fun <T> TreeNode<T>.toList(): List<T> {
    return children.flatMap { it.toList() }.plus(this.value)
}

internal fun BindingGraph.ComponentNode.getComponentAnnotation(): Component {
    return componentPath()
        .currentComponent()
        .getAnnotation(Component::class)
}

internal fun BindingGraph.ComponentNode.getSubcomponentAnnotation(): Subcomponent {
    return componentPath()
        .currentComponent()
        .getAnnotation(Subcomponent::class)
}

// Extracted from https://area-51.blog/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
internal fun <T : Annotation> T.getTypesMirrorsFromClass(block: T.() -> Unit): List<TypeMirror> {
    return try {
        block()
        error("This should fail")
    } catch (ex: MirroredTypesException) {
        ex.typeMirrors.orEmpty()
    }
}

private fun <T : Annotation> DaggerTypeElement.getAnnotation(clazz: KClass<T>): T {
    return this.fold(
        { element -> element.getAnnotation(clazz.java) },
        { TODO("ksp is not supported yet") },
    )
}
