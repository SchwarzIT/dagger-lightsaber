package schwarz.it.lightsaber

import dagger.model.BindingGraph
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

data class Finding(
    val component: BindingGraph.ComponentNode,
    val message: String,
    val codePosition: CodePosition? = null,
)

data class CodePosition(
    val element: Element,
    val annotationMirror: AnnotationMirror? = null,
    val annotationValue: AnnotationValue? = null,
)

internal fun Element.toCodePosition(): CodePosition {
    return CodePosition(this)
}
