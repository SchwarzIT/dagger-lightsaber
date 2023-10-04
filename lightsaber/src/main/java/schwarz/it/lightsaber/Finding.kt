@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package schwarz.it.lightsaber

import com.sun.tools.javac.model.JavacElements
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.util.DiagnosticSource
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.util.Elements

data class Finding(
    val message: String,
    val codePosition: CodePosition,
)

data class CodePosition(
    val path: String,
    val line: Int,
    val column: Int,
) {
    override fun toString() = "$path:$line:$column"
}

internal fun Elements.getCodePosition(
    element: Element,
    annotationMirror: AnnotationMirror? = null,
    annotationValue: AnnotationValue? = null,
): CodePosition {
    val pair = (this as JavacElements).getTreeAndTopLevel(element, annotationMirror, annotationValue)
    val sourceFile = (pair.snd as JCTree.JCCompilationUnit).sourcefile
    val diagnosticSource = DiagnosticSource(sourceFile, null)
    val line = diagnosticSource.getLineNumber(pair.fst.pos)
    val column = diagnosticSource.getColumnNumber(pair.fst.pos, true)
    return CodePosition(sourceFile.name, line, column)
}
