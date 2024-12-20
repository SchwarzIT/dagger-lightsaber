package schwarz.it.lightsaber.checkers

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.converter.ConvertWith
import org.junit.jupiter.params.provider.CsvSource
import schwarz.it.lightsaber.createSource
import schwarz.it.lightsaber.utils.AbstractCompilerArgumentConverter
import schwarz.it.lightsaber.utils.CompilationResult
import schwarz.it.lightsaber.utils.KotlinCompiler
import schwarz.it.lightsaber.utils.Rule
import schwarz.it.lightsaber.utils.assertHasFinding
import schwarz.it.lightsaber.utils.assertNoFindings
import schwarz.it.lightsaber.utils.extension

internal class UnusedInjectKtTest {

    @ParameterizedTest
    @CsvSource("kapt,4,14", "ksp,6,")
    fun injectNotUsed(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
        line: Int,
        column: Int?,
    ) {
        val module = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class MyModule {

                    @Provides
                    fun provideFoo() = Foo()
                }
            """.trimIndent(),
        )

        val foo = createSource(
            """
                package test

                import javax.inject.Inject

                class Foo 
                @Inject
                constructor()
            """.trimIndent(),
        )

        val compilation = compiler
            .compile(module, foo)

        compilation.assertUnusedInject(
            message = "The @Inject in `test.Foo` constructor is unused because there is a @Provides defined in `test.MyModule.provideFoo`.",
            line = line,
            column = column,
        )
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun injectNotDeclared(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val module = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class MyModule {

                    @Provides
                    fun provideFoo() = Foo()
                }
            """.trimIndent(),
        )

        val foo = createSource(
            """
                package test

                class Foo
            """.trimIndent(),
        )

        val compilation = compiler
            .compile(module, foo)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun injectUsed(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val foo = createSource(
            """
                package test

                import javax.inject.Inject

                class Foo @Inject constructor()
            """.trimIndent(),
        )

        val compilation = compiler
            .compile(foo)

        compilation.assertNoFindings()
    }

    @ParameterizedTest
    @CsvSource("kapt", "ksp")
    fun suppressInject(
        @ConvertWith(CompilerArgumentConverter::class) compiler: KotlinCompiler,
    ) {
        val module = createSource(
            """
                package test

                import dagger.Module
                import dagger.Provides

                @Module
                class MyModule {
                    @Provides
                    fun provideFoo() = Foo()
                }
            """.trimIndent(),
        )

        val foo = createSource(
            """
                package test

                import javax.inject.Inject
                class Foo @Suppress("UnusedInject") @Inject constructor()
            """.trimIndent(),
        )

        val compilation = compiler
            .compile(module, foo)

        compilation.assertNoFindings()
    }

    private class CompilerArgumentConverter : AbstractCompilerArgumentConverter(Rule.UnusedInject)
}

private fun CompilationResult.assertUnusedInject(message: String, line: Int, column: Int?) {
    assertHasFinding(
        message = message,
        line = line,
        column = column,
        ruleName = "UnusedInject",
        fileName = sourcesDir.resolve("test/Foo.${type.extension}").toString(),
    )
}
