package schwarz.it.lightsaber

import com.google.common.truth.Truth.assertThat
import com.google.testing.compile.Compilation
import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler
import dagger.internal.codegen.ComponentProcessor

internal fun createCompiler(
    unusedBindInstance: ReportType = ReportType.Ignore,
    unusedBindsAndProvides: ReportType = ReportType.Ignore,
    unusedDependencies: ReportType = ReportType.Ignore,
    unusedModules: ReportType = ReportType.Ignore,
): Compiler {
    return Compiler.javac()
        .withProcessors(ComponentProcessor.forTesting(LightsaberBindingGraphPlugin()))
        .withOptions(
            createOptions(
                unusedBindInstance = unusedBindInstance,
                unusedBindsAndProvides = unusedBindsAndProvides,
                unusedDependencies = unusedDependencies,
                unusedModules = unusedModules,
            ),
        )
}

internal fun createOptions(
    unusedBindInstance: ReportType = ReportType.Ignore,
    unusedBindsAndProvides: ReportType = ReportType.Ignore,
    unusedDependencies: ReportType = ReportType.Ignore,
    unusedModules: ReportType = ReportType.Ignore,
): List<String> {
    return listOf(
        "Lightsaber.UnusedBindInstance" to unusedBindInstance.toOption(),
        "Lightsaber.UnusedBindsAndProvides" to unusedBindsAndProvides.toOption(),
        "Lightsaber.UnusedDependencies" to unusedDependencies.toOption(),
        "Lightsaber.UnusedModules" to unusedModules.toOption(),
    ).map { (first, second) -> "-A$first=$second" } // https://docs.oracle.com/javase/7/docs/technotes/tools/solaris/javac.html
}

private fun ReportType.toOption(): String {
    return when (this) {
        ReportType.Ignore -> "ignore"
        ReportType.Warning -> "warning"
        ReportType.Error -> "error"
    }
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
        generatedFiles().filter { it.name.endsWith(".lightsaber") }.map { it.getCharContent(true) }.joinToString(""),
    ).isEqualTo(findingsInfo.joinToString("\n", postfix = "\n") { it.toString() })
}

internal fun Compilation.assertNoFindings() {
    assertThat(this).succeededWithoutWarnings()
    assertThat(
        generatedFiles().filter { it.name.endsWith(".lightsaber") },
    ).isEmpty()
}

internal data class FindingInfo(
    val message: String,
    val line: Int,
    val column: Int,
    val ruleName: String,
    val fileName: String = "test/MyComponent.java",
) {
    override fun toString(): String {
        return "$fileName:$line:$column: $message [$ruleName]"
    }
}
