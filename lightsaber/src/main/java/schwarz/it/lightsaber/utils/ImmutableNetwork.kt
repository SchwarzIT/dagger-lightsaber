@file:Suppress("UnstableApiUsage")

package schwarz.it.lightsaber.utils

import com.google.common.graph.ImmutableNetwork

fun <N : Any> ImmutableNetwork<N, *>.allSuccessors(node: N): Set<N> {
    return buildSet { allSuccessors(node, this) }
}

private fun <N : Any> ImmutableNetwork<N, *>.allSuccessors(node: N, successors: MutableSet<N>) {
    val currentSuccessors = successors(node)

    currentSuccessors.forEach {
        if (it !in successors) {
            successors.add(it)
            allSuccessors(it, successors)
        }
    }
}
