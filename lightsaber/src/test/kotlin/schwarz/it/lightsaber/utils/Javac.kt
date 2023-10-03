package schwarz.it.lightsaber.utils

import com.google.common.truth.Truth.assertThat
import com.google.testing.compile.Compilation
import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler
import dagger.internal.codegen.ComponentProcessor
import schwarz.it.lightsaber.LightsaberBindingGraphPlugin

internal fun createCompiler(
    checkUnusedBindInstance: Boolean = false,
    checkUnusedBindsAndProvides: Boolean = false,
    checkUnusedDependencies: Boolean = false,
    checkUnusedModules: Boolean = false,
): Compiler {
    return Compiler.javac()
        .withProcessors(ComponentProcessor.withTestPlugins(LightsaberBindingGraphPlugin()))
        .withOptions(
            createOptions(
                checkUnusedBindInstance = checkUnusedBindInstance,
                checkUnusedBindsAndProvides = checkUnusedBindsAndProvides,
                checkUnusedDependencies = checkUnusedDependencies,
                checkUnusedModules = checkUnusedModules,
            ),
        )
}

private fun createOptions(
    checkUnusedBindInstance: Boolean = false,
    checkUnusedBindsAndProvides: Boolean = false,
    checkUnusedDependencies: Boolean = false,
    checkUnusedModules: Boolean = false,
): List<String> {
    return listOf(
        "Lightsaber.CheckUnusedBindInstance" to checkUnusedBindInstance,
        "Lightsaber.CheckUnusedBindsAndProvides" to checkUnusedBindsAndProvides,
        "Lightsaber.CheckUnusedDependencies" to checkUnusedDependencies,
        "Lightsaber.CheckUnusedModules" to checkUnusedModules,
    ).map { (first, second) -> "-A$first=$second" } // https://docs.oracle.com/javase/7/docs/technotes/tools/solaris/javac.html
}

internal fun Compilation.assertHasFinding(
    message: String,
    line: Int,
    column: Int,
    fileName: String = "test/MyComponent.java",
    ruleName: String,
) {
    assertHasFindings(
        FindingInfo(
            message = message,
            line = line,
            column = column,
            ruleName = ruleName,
            fileName = fileName,
        ),
    )
}

internal fun Compilation.assertHasFindings(
    vararg findingsInfo: FindingInfo,
) {
    assertThat(this).succeededWithoutWarnings()
    assertThat(
        generatedFiles().filter { it.name.endsWith(".lightsaber") }.joinToString("") { it.getCharContent(true) },
    ).isEqualTo(findingsInfo.joinToString("\n", postfix = "\n") { it.toString() })
}

internal fun Compilation.assertNoFindings() {
    assertThat(this).succeededWithoutWarnings()
    assertThat(generatedFiles().filter { it.name.endsWith(".lightsaber") }).isEmpty()
}
