package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSDeclarationContainer
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSName
import dagger.spi.model.BindingGraph
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.toCodePosition
import schwarz.it.lightsaber.utils.getElements
import javax.inject.Inject

internal fun checkUnusedInject(
    daggerProcessingEnv: DaggerProcessingEnv,
): List<Finding> {
    val ksName = daggerProcessingEnv.resolver().getKSNameFromString("test.Foo")
    val classByName = daggerProcessingEnv.resolver().getClassDeclarationByName(ksName)

    return daggerProcessingEnv.resolver().getAllFiles()
        .map {
            it.getAllDeclarations().onEach {
                println("$it ${it.annotations.map { it.shortName.getQualifier() }.toList()}")
            }.filter {
                "javax.inject.Inject" in it.annotations.map { it.shortName.getQualifier() }
            }
        }.flatten()
        .map { Finding("This Inject is unused", it.location.toCodePosition()) }.toList()
}

fun KSDeclarationContainer.getAllDeclarations(): Sequence<KSDeclaration> {
    return sequence {
        yieldAll(declarations)
        declarations.forEach {
            if(it is KSDeclarationContainer) {
                yieldAll(it.getAllDeclarations())
            }
        }
    }
}
