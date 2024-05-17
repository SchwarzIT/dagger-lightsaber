package schwarz.it.lightsaber.checkers

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSDeclarationContainer
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import dagger.spi.model.DaggerProcessingEnv
import schwarz.it.lightsaber.Finding
import schwarz.it.lightsaber.toCodePosition

internal fun checkUnusedInject(
    daggerProcessingEnv: DaggerProcessingEnv,
): List<Finding> {
    val definedInjects = daggerProcessingEnv.resolver().getAllFiles()
        .map { file ->
            file.getAllDeclarations()
                //.onEach { println("$it ${it.annotations.map { it.shortName.asString() }.toList()}") }
                .filter { declaration -> "Inject" in declaration.annotations.map { it.shortName.asString() } }
                .map { it as KSFunctionDeclaration }
        }
        .flatten()

    val getAllProvides = daggerProcessingEnv.resolver().getAllFiles()
        .map { file ->
            file.getAllDeclarations()
                .filter { declaration -> "Provides" in declaration.annotations.map { it.shortName.asString() } }
                .map { it as KSFunctionDeclaration }
                .mapNotNull { it.returnType?.resolve() }
        }
        .flatten()
        .toSet()

    return definedInjects
        .filter { it.returnType?.resolve() in getAllProvides }
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
