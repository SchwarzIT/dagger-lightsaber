package es.lidlplus.libs.lightsaber

import com.google.testing.compile.JavaFileObjects
import org.intellij.lang.annotations.Language
import javax.tools.JavaFileObject

internal fun createSource(@Language("java") code: String): JavaFileObject {
    return JavaFileObjects.forSourceString(code.findFullQualifiedName(), code)
}

private fun String.findFullQualifiedName(): String {
    val packageRegex = "package (.*);".toRegex()
    val packageName = packageRegex.find(this)?.groupValues?.get(1)
    val objectRegex = "public (abstract )?(class|interface) ([^ ]*)".toRegex()
    val objectMatcher = checkNotNull(objectRegex.find(this)) { "No public class/interface found" }
    val objectName = objectMatcher.groupValues[3]
    return if (packageName != null) {
        "$packageName.$objectName"
    } else {
        objectName
    }
}
