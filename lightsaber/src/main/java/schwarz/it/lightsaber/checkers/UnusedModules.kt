package schwarz.it.lightsaber.checkers

import dagger.model.BindingGraph
import schwarz.it.lightsaber.Issue
import schwarz.it.lightsaber.utils.TreeNode
import schwarz.it.lightsaber.utils.getDeclaredModules
import schwarz.it.lightsaber.utils.getUsedModules
import javax.lang.model.element.Element
import javax.lang.model.util.Types

internal fun checkUnusedModules(
    bindingGraph: BindingGraph,
    types: Types,
): List<Issue> {
    val used = bindingGraph.getUsedModules()
    return bindingGraph.componentNodes()
        .flatMap { component ->
            component.getDeclaredModules(bindingGraph, types)
                .flatMap { getErrorMessages(used, it, types) }
                .map { errorMessage -> Issue(component, errorMessage) }
        }
}

private fun getErrorMessages(
    used: Set<Element>,
    node: TreeNode<Element>,
    types: Types,
    path: List<String> = emptyList(),
): List<String> {
    return buildList {
        if (!used.contains(node.value)) {
            val prefix = if (path.isEmpty()) {
                "The @Module `${node.value}`"
            } else {
                "The @Module `${node.value}` included by `${path.joinToString(" → ")}`"
            }
            val usedChildren = findUsedChildren(used, node)
            when (usedChildren.size) {
                0 -> add("$prefix is not used.")
                1 -> add("$prefix is not used but its child `${usedChildren.single().value}` is used.")
                else -> add("$prefix is not used but its children ${usedChildren.joinToString { "`${it.value}`" }} are used.")
            }
            val newPath = path.plus(node.value.toString())
            addAll(usedChildren.flatMap { child -> getErrorMessages(used, child, types, newPath) })
        } else {
            val newPath = path.plus(node.value.toString())
            addAll(node.children.flatMap { child -> getErrorMessages(used, child, types, newPath) })
        }
    }
}

private fun findUsedChildren(
    used: Set<Element>,
    node: TreeNode<Element>,
): List<TreeNode<Element>> {
    return node.children.flatMap {
        if (used.contains(it.value)) {
            listOf(it)
        } else {
            findUsedChildren(used, it)
        }
    }
}
