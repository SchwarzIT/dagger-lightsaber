@file:OptIn(ExperimentalCompilerApi::class)

package schwarz.it.lightsaber.utils

import com.google.common.truth.Truth.assertThat
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import dagger.internal.codegen.ComponentProcessor
import dagger.internal.codegen.KspComponentProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import schwarz.it.lightsaber.LightsaberDaggerProcessor
import schwarz.it.lightsaber.LightsaberJavacProcessor
import schwarz.it.lightsaber.LightsaberKspProcessorProvider
import schwarz.it.lightsaber.truth.assertThat
import java.io.File

internal interface KotlinCompiler {
    fun compile(vararg sourceFiles: SourceFile): CompilationResult
}

internal class KaptKotlinCompiler(
    vararg rules: Rule,
) : KotlinCompiler {
    private val compiler = KotlinCompilation().apply {
        languageVersion = "1.9"
        inheritClassPath = true
        annotationProcessors = listOf(
            ComponentProcessor.withTestPlugins(LightsaberDaggerProcessor()),
            LightsaberJavacProcessor(),
        )
        kaptArgs = getLightsaberArguments(*rules)
        verbose = false
    }

    override fun compile(vararg sourceFiles: SourceFile): CompilationResult {
        compiler.sources = sourceFiles.asList()
        return CompilationResult(
            compiler.compile(),
            compiler.workingDir.resolve("lightsaber").listFiles().orEmpty().asList(),
            compiler.kaptStubsDir,
            CompilationResult.Type.Kapt,
        )
    }
}

internal class KspKotlinCompiler(
    vararg rules: Rule,
) : KotlinCompiler {
    private val compiler = KotlinCompilation().apply {
        languageVersion = "1.9"
        inheritClassPath = true
        symbolProcessorProviders = mutableListOf(
            KspComponentProcessor.Provider.withTestPlugins(LightsaberDaggerProcessor()).toSymbolProcessorProvider(),
            LightsaberKspProcessorProvider(),
        )
        kspProcessorOptions = getLightsaberArguments(*rules)
        kspWithCompilation = true
        verbose = false
    }

    override fun compile(vararg sourceFiles: SourceFile): CompilationResult {
        compiler.sources = sourceFiles.asList()
        return CompilationResult(
            compiler.compile(),
            compiler.workingDir.resolve("lightsaber").listFiles().orEmpty().asList(),
            compiler.workingDir.resolve("sources"),
            CompilationResult.Type.Ksp,
        )
    }
}

private fun KspComponentProcessor.Provider.toSymbolProcessorProvider(): SymbolProcessorProvider {
    return SymbolProcessorProvider(::create)
}

enum class Rule {
    EmptyComponents,
    UnusedBindsInstances,
    UnusedBindsAndProvides,
    UnusedDependencies,
    UnusedInject,
    UnusedMembersInjectionMethods,
    UnusedModules,
    UnusedScopes,
}

internal data class CompilationResult(
    val result: JvmCompilationResult,
    val generatedFiles: List<File>,
    val sourcesDir: File,
    val type: Type,
) {
    enum class Type {
        Kapt,
        Ksp,
    }
}

internal val CompilationResult.Type.extension
    get() = when (this) {
        CompilationResult.Type.Kapt -> "java"
        CompilationResult.Type.Ksp -> "kt"
    }

private fun KotlinCompilation.getLightsaberArguments(
    vararg rules: Rule,
): MutableMap<String, String> {
    return Rule.entries
        .associate { "Lightsaber.Check${it.name}" to (it in rules).toString() }
        .plus("Lightsaber.path" to workingDir.resolve("lightsaber").absolutePath)
        .toMutableMap()
}

internal fun CompilationResult.assertHasFinding(
    message: String,
    line: Int,
    column: Int?,
    fileName: String,
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
    assertThat(lightsaberFiles().flatMap { it.readLines() })
        .containsExactlyElementsIn(findingsInfo.map { it.toString() })
}

internal fun CompilationResult.assertNoFindings() {
    assertThat(result).succeeded()
    assertThat(lightsaberFiles()).isEmpty()
}

private fun CompilationResult.lightsaberFiles(): List<File> {
    return generatedFiles.filter { it.extension == "lightsaber" }
}
