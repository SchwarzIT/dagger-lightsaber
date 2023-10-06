package schwarz.it.lightsaber.checkers

import dagger.spi.model.BindingGraph
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.CodePosition
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.domain.Module
import schwarz.it.lightsaber.utils.TreeNode
import schwarz.it.lightsaber.utils.getDeclaredModules
import schwarz.it.lightsaber.utils.getModulesCodePosition
import schwarz.it.lightsaber.utils.getUsedModules

internal fun checkUnusedModules(
    bindingGraph: BindingGraph,
    daggerProcessingEnv: DaggerProcessingEnv,
): List<Finding> {
    val used = bindingGraph.getUsedModules()
    return bindingGraph.componentNodes()
        .flatMap { component ->
            component.getDeclaredModules(bindingGraph, daggerProcessingEnv)
                .flatMap {
                    getErrorMessages(
                        used = used,
                        node = it,
                        daggerProcessingEnv = daggerProcessingEnv,
                        codePosition = { component.getModulesCodePosition(daggerProcessingEnv) },
                    )
                }
                .map { (errorMessage, codePosition) -> Finding(errorMessage, codePosition) }
        }
}

private fun getErrorMessages(
    used: Set<Module>,
    node: TreeNode<Module>,
    daggerProcessingEnv: DaggerProcessingEnv,
    codePosition: () -> CodePosition,
    path: List<String> = emptyList(),
): List<Pair<String, CodePosition>> {
    return buildList {
        if (!used.contains(node.value)) {
            val usedChildren = findUsedChildren(used, node)
            add(
                generateMessage(
                    usedChildren.map { it.value },
                    node.value,
                    path,
                ) to codePosition.invoke(),
            )
            val newPath = path.plus(node.value.toString())
            addAll(
                usedChildren.flatMap { child ->
                    getErrorMessages(
                        used = used,
                        node = child,
                        daggerProcessingEnv = daggerProcessingEnv,
                        codePosition = { node.value.getIncludesCodePosition(daggerProcessingEnv) },
                        path = newPath,
                    )
                },
            )
        } else {
            val newPath = path.plus(node.value.toString())
            addAll(
                node.children.flatMap { child ->
                    getErrorMessages(
                        used = used,
                        node = child,
                        daggerProcessingEnv,
                        codePosition = { node.value.getIncludesCodePosition(daggerProcessingEnv) },
                        path = newPath,
                    )
                },
            )
        }
    }
}

private fun generateMessage(
    usedChildren: List<Module>,
    node: Module,
    path: List<String> = emptyList(),
): String {
    val prefix = if (path.isEmpty()) {
        "The @Module `$node`"
    } else {
        "The @Module `$node` included by `${path.joinToString(" → ")}`"
    }
    return when (usedChildren.size) {
        0 -> "$prefix is not used."
        1 -> "$prefix is not used but its child `${usedChildren.single()}` is used."
        else -> "$prefix is not used but its children ${usedChildren.joinToString { "`$it`" }} are used."
    }
}

private fun findUsedChildren(
    used: Set<Module>,
    node: TreeNode<Module>,
): List<TreeNode<Module>> {
    return node.children.flatMap {
        if (used.contains(it.value)) {
            listOf(it)
        } else {
            findUsedChildren(used, it)
        }
    }
}
