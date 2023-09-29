package schwarz.it.lightsaber.utils

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dagger.spi.model.DaggerElement
import dagger.spi.model.DaggerProcessingEnv
import dagger.spi.model.DaggerTypeElement
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

fun <T> DaggerProcessingEnv.fold(
    javac: (ProcessingEnvironment) -> T,
    ksp: (SymbolProcessorEnvironment) -> T,
): T = when (backend()!!) {
    DaggerProcessingEnv.Backend.JAVAC -> javac(javac())
    DaggerProcessingEnv.Backend.KSP -> ksp(ksp())
}

fun <T> DaggerTypeElement.fold(
    javac: (TypeElement) -> T,
    ksp: (KSClassDeclaration) -> T,
): T = when (backend()!!) {
    DaggerProcessingEnv.Backend.JAVAC -> javac(javac())
    DaggerProcessingEnv.Backend.KSP -> ksp(ksp())
}

fun <T> DaggerElement.fold(
    javac: (Element) -> T,
    ksp: (KSAnnotated) -> T,
): T = when (backend()!!) {
    DaggerProcessingEnv.Backend.JAVAC -> javac(javac())
    DaggerProcessingEnv.Backend.KSP -> ksp(ksp())
}
