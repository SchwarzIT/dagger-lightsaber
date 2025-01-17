package schwarz.it.lightsaber.utils

import schwarz.it.lightsaber.Issue
import java.io.OutputStream
import java.io.PrintWriter
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

internal class FileGenerator(private val path: Path) {
    fun createFile(fileName: String): OutputStream {
        path.createDirectories()
        return path.resolve(fileName).outputStream()
    }
}

internal fun FileGenerator.writeFile(fileName: String, issues: List<Issue>) {
    this.createFile("$fileName.lightsaber")
        .let(::PrintWriter)
        .use { writer -> issues.forEach { writer.println("${it.codePosition}: ${it.message} [${it.rule}]") } }
}
