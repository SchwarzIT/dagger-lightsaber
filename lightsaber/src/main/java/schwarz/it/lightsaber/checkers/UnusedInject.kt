package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSDeclarationContainer
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.toCodePosition

internal fun checkUnusedInject(
    daggerProcessingEnv: DaggerProcessingEnv,
): List<Finding> {
    return daggerProcessingEnv.resolver().getAllFiles()
        .map { file ->
            file.getAllDeclarations()
                //.onEach { println("$it ${it.annotations.map { it.shortName.asString() }.toList()}") }
                .filter { "Inject" in it.annotations.map { it.shortName.asString() } }
        }
        .flatten()
        .map { Finding("This Inject is unused", it.location.toCodePosition()) }
        .toList()
}

fun KSDeclarationContainer.getAllDeclarations(): Sequence<KSDeclaration> {
    return sequence {
        yieldAll(declarations)
        declarations.forEach {
            if (it is KSDeclarationContainer) {
                yieldAll(it.getAllDeclarations())
            }
        }
    }
}
