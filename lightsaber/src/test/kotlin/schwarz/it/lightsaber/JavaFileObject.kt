package schwarz.it.lightsaber

import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language

internal fun createSource(@Language("kotlin") code: String): SourceFile {
    return SourceFile.kotlin("${code.findFullQualifiedName()}.kt", code)
}

private fun String.findFullQualifiedName(): String {
    val packageRegex = "package (.*)".toRegex()
    val packageName = packageRegex.find(this)?.groupValues?.get(1)
    val objectRegex = "(abstract )?(class|interface|object) ([^ ]*)".toRegex()
    val objectMatcher = checkNotNull(objectRegex.find(this)) { "No class/interface/object found" }
    val objectName = objectMatcher.groupValues[3]
    return if (packageName != null) {
        "${packageName.replace(".", "/")}/$objectName"
    } else {
        objectName
    }
}
