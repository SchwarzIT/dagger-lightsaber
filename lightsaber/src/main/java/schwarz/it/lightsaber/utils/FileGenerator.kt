package schwarz.it.lightsaber.utils

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.Issue
import schwarz.it.lightsaber.getMessage
import java.io.OutputStream
import java.io.PrintWriter
import javax.annotation.processing.Filer
import javax.tools.StandardLocation

internal interface FileGenerator {
    fun createFile(packageName: String, fileName: String, extension: String): OutputStream

    companion object {
        operator fun invoke(processingEnv: DaggerProcessingEnv): FileGenerator {
            return processingEnv.fold({ FileGenerator(it.filer) }, { FileGenerator(it.codeGenerator) })
        }

        operator fun invoke(codeGenerator: CodeGenerator): FileGenerator {
            return FileGeneratorKsp(codeGenerator)
        }

        operator fun invoke(filer: Filer): FileGenerator {
            return FileGeneratorJavac(filer)
        }
    }
}

private class FileGeneratorJavac(private val filer: Filer) : FileGenerator {
    override fun createFile(packageName: String, fileName: String, extension: String): OutputStream {
        return filer
            .createResource(StandardLocation.CLASS_OUTPUT, packageName, "$fileName.$extension")
            .openOutputStream()
    }
}

private class FileGeneratorKsp(private val codeGenerator: CodeGenerator) : FileGenerator {
    override fun createFile(packageName: String, fileName: String, extension: String): OutputStream {
        return codeGenerator.createNewFile(Dependencies.ALL_FILES, packageName, fileName, extension)
    }
}

internal fun FileGenerator.writeFile(fileName: String, issues: List<Issue>) {
    this.createFile("schwarz.it.lightsaber", fileName, "lightsaber")
        .let(::PrintWriter)
        .use { writer -> issues.forEach { writer.println(it.getMessage()) } }
}
