package schwarz.it.lightsaber

import dagger.model.BindingGraph

data class Issue(
    val component: BindingGraph.ComponentNode,
    val message: String,
)
