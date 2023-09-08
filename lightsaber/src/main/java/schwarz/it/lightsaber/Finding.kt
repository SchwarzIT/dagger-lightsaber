package schwarz.it.lightsaber

import dagger.model.BindingGraph
import javax.lang.model.element.Element

data class Finding(
    val component: BindingGraph.ComponentNode,
    val message: String,
    val element: Element? = null,
)
