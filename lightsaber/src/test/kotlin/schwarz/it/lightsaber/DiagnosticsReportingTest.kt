@file:OptIn(ExperimentalCompilerApi::class)

package schwarz.it.lightsaber

import com.google.common.truth.Truth.assertThat
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.tschuchort.compiletesting.useKsp2
import dagger.internal.codegen.ComponentProcessor
import dagger.internal.codegen.KspComponentProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import schwarz.it.lightsaber.utils.Rule

internal class DiagnosticsReportingTest {

    private val module = createSource(
        """
            package test

            import dagger.Module
            import dagger.Provides

            @Module
            class MyModule {
                @Provides
                fun dependency(): String {
                    return "string"
                }
            }
        """.trimIndent(),
    )

    private val component = createSource(
        """
            package test

            import dagger.Component

            @Component(modules = [MyModule::class])
            interface MyComponent
        """.trimIndent(),
    )

    private val unusedInject = listOf(
        createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class FooModule {
                    @Provides
                    fun provideFoo() = Foo()
                }
            """.trimIndent(),
        ),
        createSource(
            """
                package test

                import javax.inject.Inject

                class Foo
                @Inject
                constructor()
            """.trimIndent(),
        ),
    )

    @ParameterizedTest
    @ValueSource(strings = ["kapt", "ksp"])
    fun unusedModule_reportedAsError(backend: String) {
        val result = compile(backend, Rule.UnusedModules, emptyMap(), module, component)

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The @Module `test.MyModule` is not used. [UnusedModules]")
    }

    @ParameterizedTest
    @ValueSource(strings = ["kapt", "ksp"])
    fun unusedModule_reportedAsWarning(backend: String) {
        val result = compile(
            backend,
            Rule.UnusedModules,
            mapOf("Lightsaber.Severity.UnusedModules" to "warning"),
            module,
            component,
        )

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        assertThat(result.messages).contains("The @Module `test.MyModule` is not used. [UnusedModules]")
    }

    @ParameterizedTest
    @ValueSource(strings = ["kapt", "ksp"])
    fun unusedModule_ignored(backend: String) {
        val result = compile(
            backend,
            Rule.UnusedModules,
            mapOf("Lightsaber.Severity.UnusedModules" to "ignore"),
            module,
            component,
        )

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        assertThat(result.messages).doesNotContain("[UnusedModules]")
    }

    @ParameterizedTest
    @ValueSource(strings = ["kapt", "ksp"])
    fun severityOfOtherRuleDoesNotAffectRule(backend: String) {
        val result = compile(
            backend,
            Rule.UnusedModules,
            mapOf("Lightsaber.Severity.UnusedBindsAndProvides" to "warning"),
            module,
            component,
        )

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
    }

    @ParameterizedTest
    @ValueSource(strings = ["kapt", "ksp"])
    fun unusedInject_reportedAsError(backend: String) {
        val result = compile(backend, Rule.UnusedInject, emptyMap(), *unusedInject.toTypedArray())

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("[UnusedInject]")
    }

    @ParameterizedTest
    @ValueSource(strings = ["kapt", "ksp"])
    fun unusedInject_reportedAsWarning(backend: String) {
        val result = compile(
            backend,
            Rule.UnusedInject,
            mapOf("Lightsaber.Severity.UnusedInject" to "warning"),
            *unusedInject.toTypedArray(),
        )

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
        assertThat(result.messages).contains("[UnusedInject]")
    }

    private fun compile(
        backend: String,
        rule: Rule,
        severities: Map<String, String>,
        vararg sources: SourceFile,
    ): JvmCompilationResult {
        val options = Rule.entries
            .associate { "Lightsaber.Check${it.name}" to (it == rule).toString() }
            .plus("Lightsaber.Report" to "diagnostics")
            .plus(severities)
        val compilation = KotlinCompilation().apply {
            languageVersion = "2.0"
            inheritClassPath = true
            verbose = false
            this.sources = sources.asList()
        }
        when (backend) {
            "kapt" -> compilation.apply {
                annotationProcessors = listOf(
                    ComponentProcessor.withTestPlugins(LightsaberDaggerProcessor()),
                    LightsaberJavacProcessor(),
                )
                kaptArgs = options.toMutableMap()
            }

            "ksp" -> compilation.apply {
                useKsp2()
                symbolProcessorProviders = mutableListOf(
                    KspComponentProcessor.Provider
                        .withTestPlugins(LightsaberDaggerProcessor())
                        .let { SymbolProcessorProvider(it::create) },
                    LightsaberKspProcessorProvider(),
                )
                kspProcessorOptions = options.toMutableMap()
                kspWithCompilation = true
            }

            else -> throw IllegalArgumentException("Unknown backend $backend")
        }
        return compilation.compile()
    }
}
