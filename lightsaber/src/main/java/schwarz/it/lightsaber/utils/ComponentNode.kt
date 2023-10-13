package schwarz.it.lightsaber.utils

import com.google.devtools.ksp.symbol.KSClassDeclaration
import dagger.Component
import dagger.Subcomponent
import dagger.spi.model.BindingGraph
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.domain.Module
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror

internal fun BindingGraph.ComponentNode.getDeclaredModules(
    bindingGraph: BindingGraph,
    daggerProcessingEnv: DaggerProcessingEnv,
): List<TreeNode<Module>> {
    val usedModules = bindingGraph.getUsedModules()
    return if (isSubcomponent) {
        componentPath().currentComponent()
            .fold(
                { element ->
                    element.getAnnotation(Subcomponent::class.java)
                        .getTypesMirrorsFromClass { modules }
                        .map { Module(daggerProcessingEnv, it) }
                },
                { ksDeclaration ->
                    ksDeclaration
                        .getDeclaredArguments(dagger.Subcomponent::class, "modules")
                        .map { Module(it.declaration as KSClassDeclaration) }
                },
            )
    } else {
        componentPath().currentComponent()
            .fold(
                { element ->
                    element.getAnnotation(Component::class.java)
                        .getTypesMirrorsFromClass { modules }
                        .map { Module(daggerProcessingEnv, it) }
                },
                { ksDeclaration ->
                    ksDeclaration
                        .getDeclaredArguments(dagger.Component::class, "modules")
                        .map { Module(it.declaration as KSClassDeclaration) }
                },
            )
    }.map { module -> getModuleTree(usedModules, module, daggerProcessingEnv) }
}

private fun getModuleTree(
    usedModules: Set<Module>,
    module: Module,
    daggerProcessingEnv: DaggerProcessingEnv,
): TreeNode<Module> {
    return TreeNode(
        value = module,
        children = module.getIncludedModules(daggerProcessingEnv)
            .map { getModuleTree(usedModules, it, daggerProcessingEnv) },
    )
}

class TreeNode<T>(val value: T, val children: List<TreeNode<T>> = emptyList())

internal fun <T> TreeNode<T>.toList(): List<T> {
    return children.flatMap { it.toList() }.plus(this.value)
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

internal fun BindingGraph.ComponentNode.getFullQualifiedName(): String {
    return this.componentPath().currentComponent()
        .fold(
            { it.qualifiedName.toString() },
            { it.qualifiedName!!.asString() },
        )
}
