package schwarz.it.lightsaber.utils

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import dagger.spi.model.DaggerProcessingEnv
import java.io.OutputStream
import javax.annotation.processing.Filer
import javax.tools.StandardLocation

internal interface FileGenerator {
    fun createFile(packageName: String, fileName: String, extension: String): OutputStream

    companion object {
        operator fun invoke(processingEnv: DaggerProcessingEnv): FileGenerator {
            return processingEnv.fold({ FileGeneratorJavac(it.filer) }, { FileGeneratorKsp(it.codeGenerator) })
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
