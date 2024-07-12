@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package schwarz.it.lightsaber

import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.NonExistLocation
import com.sun.tools.javac.model.JavacElements
import com.sun.tools.javac.util.DiagnosticSource
import schwarz.it.lightsaber.domain.Suppression
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.util.Elements

data class Finding(
    val message: String,
    val codePosition: CodePosition,
    val suppression: Suppression,
)

data class CodePosition(
    val path: String,
    val line: Int,
    val column: Int? = null,
) {
    override fun toString() = if (column != null) "$path:$line:$column" else "$path:$line"

    companion object {
        val Unknown = CodePosition("Unknown", 0)
    }
}

data class Issue(
    val codePosition: CodePosition,
    val message: String,
    val rule: String,
)

internal fun Elements.getCodePosition(
    element: Element,
    annotationMirror: AnnotationMirror? = null,
    annotationValue: AnnotationValue? = null,
): CodePosition {
    return try {
        this as JavacElements
        val pair = getTreeAndTopLevel(element, annotationMirror, annotationValue) ?: return CodePosition.Unknown
        val sourceFile = pair.snd.sourcefile
        val diagnosticSource = DiagnosticSource(sourceFile, null)
        val line = diagnosticSource.getLineNumber(pair.fst.pos)
        val column = diagnosticSource.getColumnNumber(pair.fst.pos, true)
        CodePosition(sourceFile.name, line, column)
    } catch (_: IllegalAccessError) {
        println("w: To get the correct issue position you should run the compilation with `--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED`. More information here: https://github.com/SchwarzIT/dagger-lightsaber/issues/102  [Lightsaber]")
        CodePosition.Unknown
    }
}

internal fun Location.toCodePosition(): CodePosition {
    return when (this) {
        is FileLocation -> CodePosition(filePath, lineNumber)
        NonExistLocation -> CodePosition.Unknown
    }
}
