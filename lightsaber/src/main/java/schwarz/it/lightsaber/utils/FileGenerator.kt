package schwarz.it.lightsaber.utils

import dagger.spi.model.DaggerProcessingEnv
import java.io.OutputStream
import javax.annotation.processing.Filer
import javax.tools.StandardLocation

internal interface FileGenerator {
    fun createFile(packageName: String, fileName: String, extension: String): OutputStream

    companion object {
        operator fun invoke(processingEnv: DaggerProcessingEnv): FileGenerator {
            return processingEnv.fold({ FileGeneratorJavac(it.filer) }, { TODO("ksp is not supported yet") })
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
