package schwarz.it.lightsaber

import dagger.model.BindingGraph

data class Finding(
    val component: BindingGraph.ComponentNode,
    val message: String,
)
