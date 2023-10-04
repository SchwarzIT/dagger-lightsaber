package schwarz.it.lightsaber.utils

import com.google.common.truth.Truth
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import dagger.internal.codegen.KspComponentProcessor
import schwarz.it.lightsaber.LightsaberBindingGraphPlugin
import schwarz.it.lightsaber.truth.assertThat
import java.io.File

internal fun createKotlinCompiler(
    checkUnusedBindInstance: Boolean = false,
    checkUnusedBindsAndProvides: Boolean = false,
    checkUnusedDependencies: Boolean = false,
    checkUnusedModules: Boolean = false,
): KotlinCompilation {
    return KotlinCompilation().apply {
        inheritClassPath = true
        symbolProcessorProviders = listOf(
            KspComponentProcessor.Provider.withTestPlugins(LightsaberBindingGraphPlugin()),
        )
        kspArgs = mutableMapOf(
            "Lightsaber.CheckUnusedBindInstance" to checkUnusedBindInstance.toString(),
            "Lightsaber.CheckUnusedBindsAndProvides" to checkUnusedBindsAndProvides.toString(),
            "Lightsaber.CheckUnusedDependencies" to checkUnusedDependencies.toString(),
            "Lightsaber.CheckUnusedModules" to checkUnusedModules.toString(),
        )
        kspWithCompilation = true
        verbose = false
    }
}

internal fun KotlinCompilation.compile(vararg sourceFiles: SourceFile): CompilationResult {
    this.sources = sourceFiles.asList()
    return CompilationResult(compile(), findGeneratedFiles(this), workingDir.resolve("sources"))
}

internal data class CompilationResult(
    val result: KotlinCompilation.Result,
    val generatedFiles: List<File>,
    val sourcesDir: File,
)

private fun findGeneratedFiles(compilation: KotlinCompilation): List<File> {
    return compilation.kspSourcesDir
        .walkTopDown()
        .filter { it.isFile }
        .toList()
}

internal fun CompilationResult.assertHasFinding(
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

internal fun CompilationResult.assertHasFindings(vararg findingsInfo: FindingInfo) {
    assertThat(result).succeeded()
    Truth.assertThat(lightsaberFiles().joinToString("") { it.readText() })
        .isEqualTo(findingsInfo.joinToString("\n", postfix = "\n") { it.toString() })
}

internal fun CompilationResult.assertNoFindings() {
    assertThat(result).succeeded()
    Truth.assertThat(lightsaberFiles()).isEmpty()
}

private fun CompilationResult.lightsaberFiles(): List<File> {
    return generatedFiles.filter { it.extension == "lightsaber" }
}
